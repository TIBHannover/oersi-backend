# How to release this project

* Create release issue https://gitlab.com/oersi/oersi-backend/-/issues/new
* Checkout the develop branch
```
git checkout develop
```
* Set release version _x.y_ in _pom.xml_ and commit
     * ...with _x_ major version, _y_ minor version
```
git add pom.xml
git commit -m "release <RELEASE-VERSION> (Ref <ISSUE-URL>)"
```
* Merge develop into master
```
git checkout master
git merge develop
```
* Create release tag
```
git tag -a <RELEASE-VERSION> -m "release <RELEASE-VERSION> (Ref <ISSUE-URL>)"
```
* Checkout the develop branch
```
git checkout develop
```
* Set snapshot version _x.y+1-SNAPSHOT_ in _pom.xml_ and commit
```
git add pom.xml
git commit -m "next snapshot (Ref <ISSUE-URL>)"
```
* Push
```
git push origin develop
git push origin master
git push origin <RELEASE-VERSION>
```