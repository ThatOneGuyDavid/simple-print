"""Publish preserved signed APKs; requires GitHub Actions GITHUB_TOKEN, never a signing key."""
import hashlib
import json
import os
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import quote, urlparse
from urllib.request import Request, urlopen

root = Path(__file__).resolve().parent.parent
repo = os.environ['GITHUB_REPOSITORY']
token = os.environ['GITHUB_TOKEN']
api = 'https://api.github.com/repos/' + repo

def call(url, payload=None, binary=False, missing_ok=False):
	headers = {'Authorization': 'Bearer ' + token, 'Accept': 'application/vnd.github+json',
		'X-GitHub-Api-Version': '2022-11-28'}
	data = None
	if payload is not None:
		data = payload if binary else json.dumps(payload).encode()
		headers['Content-Type'] = 'application/octet-stream' if binary else 'application/json'
	try:
		with urlopen(Request(url, data=data, headers=headers), timeout=90) as response:
			return json.load(response)
	except HTTPError as error:
		if missing_ok and error.code == 404:
			return None
		# Do not expose request headers or tokens in logs.
		raise RuntimeError('GitHub returned HTTP ' + str(error.code)) from None

for release in json.loads((root / 'release-manifest.json').read_text()):
	tag = 'v' + release['version']
	commit = release['commit']
	ref_url = api + '/git/ref/tags/' + tag
	ref = call(ref_url, missing_ok=True)
	if ref is None:
		call(api + '/git/refs', {'ref': 'refs/tags/' + tag, 'sha': commit})
	else:
		assert ref['object']['sha'] == commit, 'Refusing to replace an existing tag'
	published = call(api + '/releases/tags/' + tag, missing_ok=True)
	if published is None:
		published = call(api + '/releases', {
			'tag_name': tag, 'target_commitish': commit, 'name': 'Simple Print ' + tag,
			'body': release['notes'], 'draft': False, 'prerelease': False,
			'make_latest': 'true' if release['version'] == '0.10' else 'false',
		})
	assets = {a['name']: a for a in published['assets']}
	folder = root / 'artifacts' / tag
	for path in sorted(folder.iterdir()):
		if not path.is_file():
			continue
		data = path.read_bytes()
		expected = 'sha256:' + hashlib.sha256(data).hexdigest()
		if path.name in assets:
			assert assets[path.name].get('digest') == expected, 'Existing asset digest mismatch'
			continue
		upload_url = published['upload_url'].split('{')[0]
		assert urlparse(upload_url).hostname == 'uploads.github.com'
		asset = call(upload_url + '?name=' + quote(path.name), data, binary=True)
		assert asset['size'] == len(data)
		if asset.get('digest'):
			assert asset['digest'] == expected
	print('Published and checked metadata:', tag)
