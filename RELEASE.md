# How to release this project

* Use one release issue for all projects https://gitlab.com/oersi/oersi-setup/-/issues/new
* Set release version _x.y_ in _pom.xml_ and commit
     * ...with _x_ major version, _y_ minor version
```
git add pom.xml
git commit -m "release <RELEASE-VERSION> (Ref <ISSUE-URL>)"
```
* Create release tag
```
git tag -a <RELEASE-VERSION> -m "release <RELEASE-VERSION> (Ref <ISSUE-URL>)"
```
* Set snapshot version _x.y+1-SNAPSHOT_ in _pom.xml_ and commit
```
git add pom.xml
git commit -m "next snapshot (Ref <ISSUE-URL>)"
```
* Push
```
git push origin master
git push origin <RELEASE-VERSION>
```