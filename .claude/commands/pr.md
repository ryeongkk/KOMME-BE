---
description: .github/PULL_REQUEST_TEMPLATE.md 템플릿을 기반으로 GitHub PR을 생성합니다
argument-hint: [issue-number]
allowed-tools: Read, Bash(git status:*), Bash(git branch:*), Bash(git log:*), Bash(git diff:*), Bash(git push:*), Bash(gh pr create:*), Bash(gh pr view:*)
---

## 참고 템플릿

@.github/PULL_REQUEST_TEMPLATE.md

## 입력

`$ARGUMENTS` (해결하는 이슈 번호)

## 절차

1. 현재 브랜치명을 확인한다 (`git branch --show-current`). 브랜치명이 `{type}/#{issue-number}-{description}` 형식(예: `feat/#1-core-setup`)인지 확인한다.
2. `develop` 대비 커밋 로그와 diff(`git log develop..HEAD`, `git diff develop...HEAD`)를 확인해 변경 사항을 파악한다.
3. 원격에 push되어 있는지 확인하고, 안 되어 있으면 사용자에게 push 여부를 확인한 뒤 `git push -u origin {branch}`를 실행한다.
4. PR 제목은 브랜치명을 변환해 만든다: 첫 글자를 대문자로, 이슈 번호(`#N`) 뒤 첫 `-`만 공백으로, 나머지는 그대로 (예: 브랜치 `feat/#1-core-setup` → 제목 `Feat/#1 core setup`).
5. 템플릿 형식 그대로 body를 구성한다:
   - `Issue Number`: `resolved #{issue-number}` (인자로 안 주어졌으면 커밋/브랜치명에서 유추하거나 사용자에게 확인)
   - `요약(Summary)`: 커밋 로그를 바탕으로 무엇을/왜 변경했는지 bullet로 정리
   - `공유사항 to 리뷰어`: 리뷰어가 중점적으로 봐야 할 부분이나 논의할 부분을 사용자에게 물어서 채우거나, 없으면 생략
   - `PR Checklist`: 실제로 확인된 항목만 체크(`[x]`)한다. 임의로 다 체크하지 않는다.
6. base는 `develop`으로 `gh pr create --base develop --title "..." --body "..."` 를 실행한다.
7. 생성된 PR URL을 사용자에게 알려준다.
