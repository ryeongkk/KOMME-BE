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
3. `gh label list`로 저장소 라벨 목록을 확인하고, type과 이름이 일치하는 라벨(예: `chore`)이 있으면 그대로 사용하고, `feat`처럼 정확히 일치하는 라벨이 없으면 가장 가까운 라벨(예: `enhancement`)을 제안하거나 라벨 없이 진행할지 사용자에게 확인한다.
4. 템플릿 형식 그대로 body를 구성해 `gh issue create --title "..." --body "..." --label "..."` 로 이슈를 생성한다.
5. 생성된 이슈 번호와 URL을 사용자에게 알려준다.
