---
description: 컨벤션에 맞춰 변경 사항을 스테이징하고 커밋합니다
argument-hint: [message]
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git add:*), Bash(git commit:*), Bash(git log:*)
---

## 커밋 메시지 컨벤션

이 저장소는 아래 형식을 사용합니다 (기존 로그 예시 참고):

```
{type}: {변경 사항 요약(한글)}
```

예: `feat: BaseEntity 추가`, `refactor: GeneralException 수정`, `chore: PR 템플릿 설정`

- `type`: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style` 등
- 요약은 한글로, 무엇을 추가/수정했는지 간결하게 작성 (동사는 관례상 명사형으로 끝냄: "~추가", "~수정")

## 입력

`$ARGUMENTS`

## 절차

1. `git status`와 `git diff`로 변경 사항을 확인한다.
2. 변경 논리 단위가 여러 개면(예: 기능 추가 + 무관한 설정 변경) 하나의 커밋에 섞지 말고 나눠서 커밋할지 사용자에게 확인한다.
3. 변경 내용에 맞는 `type`을 판단하고, `$ARGUMENTS`에 메시지가 주어졌으면 그것을 요약에 반영, 없으면 diff를 바탕으로 요약을 제안한다.
4. 최근 `git log`의 메시지 스타일과 일치하는지 확인 후 커밋 메시지를 확정한다.
5. 관련 파일만 `git add`로 스테이징한다 (`git add -A`/`git add .` 금지, 파일을 명시적으로 지정).
6. `git commit -m "{type}: {요약}"` 로 커밋한다.
7. `git status`로 커밋 완료를 확인하고 결과를 사용자에게 알려준다.
