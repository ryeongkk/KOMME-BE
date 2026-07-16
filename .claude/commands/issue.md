---
description: .github/ISSUE_TEMPLATE/ISSUE.md 템플릿을 기반으로 GitHub 이슈를 생성합니다
argument-hint: [type] [title]
allowed-tools: Read, Bash(gh issue create:*), Bash(gh label list:*)
---

## 참고 템플릿

@.github/ISSUE_TEMPLATE/ISSUE.md

## 입력

`$ARGUMENTS`

## 절차

1. 템플릿 구조를 그대로 따라 아래 항목을 사용자에게 채우도록 요청한다 (인자로 이미 제공된 정보는 다시 묻지 않는다):
   - 작업 개요 (무엇을 왜 하는지 한 줄)
   - 작업 내용 (체크리스트, 구체적인 작업 항목들)
   - 관련 화면/기능 (홈 / 로그인·회원가입 / 코스 추천 / 마이페이지 / 기타)
   - 기타 사항 (논의 필요/고민되는 부분, 없으면 생략 가능)
2. 이슈 제목은 `[type] 제목` 형식으로 만든다 (예: `[chore] Claude 및 Codex 세팅`, `[feat] 로그인 기능 구현`).
3. 라벨은 아래 고정 매핑으로 자동 결정한다 (확인 없이 바로 적용):

   | type | label |
   |---|---|
   | feat | enhancement |
   | fix | bug |
   | refactor | enhancement |
   | chore | chore |
   | docs | documentation |
   | test | (라벨 없음) |
   | style | (라벨 없음) |

   `gh label list`로 저장소에 해당 라벨이 실제로 존재하는지만 확인하고, 매핑에 없는 type이거나 라벨이 저장소에 없으면 라벨 없이 진행한다.
4. Assignee는 항상 `@me`(현재 인증된 사용자, PR/이슈를 생성하는 사람)로 자동 지정한다.
5. 템플릿 형식 그대로 body를 구성해 `gh issue create --title "..." --body "..." --label "..." --assignee "@me"` 로 이슈를 생성한다.
6. 생성된 이슈 번호와 URL을 사용자에게 알려준다.
