---
description: .github/PULL_REQUEST_TEMPLATE.md 템플릿을 기반으로 GitHub PR을 생성합니다
argument-hint: [issue-number]
allowed-tools: Read, Bash(git status:*), Bash(git branch:*), Bash(git log:*), Bash(git diff:*), Bash(git push:*), Bash(gh pr create:*), Bash(gh pr view:*), Bash(gh label list:*)
---

## 참고 템플릿

@.github/PULL_REQUEST_TEMPLATE.md

## 입력

`$ARGUMENTS` (해결하는 이슈 번호)

## 절차

1. 현재 브랜치명을 확인한다 (`git branch --show-current`). 브랜치명이 `{type}/#{issue-number}-{description}` 형식(예: `feat/#1-core-setup`)인지 확인한다.
2. `develop` 대비 커밋 로그와 diff(`git log develop..HEAD`, `git diff develop...HEAD`)를 확인해 변경 사항을 파악한다.
3. 원격에 push되어 있는지 확인하고, 안 되어 있으면 사용자에게 push 여부를 확인한 뒤 `git push -u origin {branch}`를 실행한다.
4. PR 제목은 `[Type] 기능 설명` 형식으로 만든다. `Type`은 브랜치명의 type(`feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`)에서 첫 글자만 대문자로 바꾼 것이고, 설명은 브랜치명의 description이나 커밋 로그를 바탕으로 한글로 간결하게 작성한다 (예: 브랜치 `feat/#1-core-setup` → 제목 `[Feat] 코어 세팅 추가`).
5. 템플릿 형식 그대로 body를 구성한다:
   - `Issue Number`: `resolved #{issue-number}` (인자로 안 주어졌으면 커밋/브랜치명에서 유추하거나 사용자에게 확인)
   - `요약(Summary)`: 커밋 로그를 바탕으로 무엇을/왜 변경했는지 bullet로 정리
   - `공유사항 to 리뷰어`: 리뷰어가 중점적으로 봐야 할 부분이나 논의할 부분을 사용자에게 물어서 채우거나, 없으면 생략
   - `PR Checklist`: 실제로 확인된 항목만 체크(`[x]`)한다. 임의로 다 체크하지 않는다.
6. 라벨은 `/issue`와 동일한 고정 매핑을 브랜치명의 `type`에 적용해 자동 결정한다 (확인 없이 바로 적용):

   | type | label |
   |---|---|
   | feat | enhancement |
   | fix | bug |
   | refactor | enhancement |
   | chore | chore |
   | docs | documentation |
   | test | (라벨 없음) |
   | style | (라벨 없음) |

   `gh label list`로 저장소에 해당 라벨이 실제로 존재하는지만 확인하고, 매핑에 없거나 라벨이 없으면 라벨 없이 진행한다.
7. Assignee는 항상 `@me`로 자동 지정한다.
8. base는 `develop`으로 `gh pr create --base develop --title "..." --body "..." --label "..." --assignee "@me"` 를 실행한다.
9. 생성된 PR URL을 사용자에게 알려준다.
