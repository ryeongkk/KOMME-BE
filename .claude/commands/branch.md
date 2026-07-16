---
description: 이슈 번호 기반으로 컨벤션에 맞는 브랜치를 생성하고 체크아웃합니다
argument-hint: [type] [issue-number] [description]
allowed-tools: Bash(git status:*), Bash(git branch:*), Bash(git checkout:*), Bash(git switch:*), Bash(git pull:*), Bash(gh issue view:*)
---

## 브랜치 네이밍 컨벤션

이 저장소는 아래 형식을 사용합니다 (기존 브랜치: `feat/#1-core-setup`, `chore/#3-ai-setup`):

```
{type}/#{issue-number}-{description}
```

- `type`: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style` 등
- `issue-number`: 관련 이슈 번호 (`#` 포함)
- `description`: 영어 kebab-case 요약

## 입력

`$ARGUMENTS`

인자가 부족하면(예: description 누락) 부족한 값을 사용자에게 되물어봐서 채운다. 이슈 번호만 주어졌다면 `gh issue view {number}`로 제목을 확인해 type과 description을 유추해 제안한다.

## 절차

1. `git status`로 현재 워킹트리가 깨끗한지 확인한다. 커밋되지 않은 변경이 있으면 사용자에게 알리고 계속할지 확인한다.
2. `develop` 브랜치가 최신 상태인지 확인한다 (`git checkout develop && git pull` 또는 현재 브랜치를 유지할지 사용자와 확인).
3. 컨벤션에 맞는 브랜치명을 조합한다.
4. `git checkout -b {type}/#{issue-number}-{description}` 로 브랜치를 생성 및 체크아웃한다.
5. 생성된 브랜치명을 사용자에게 알려준다. 원격 push는 별도로 요청받기 전까지 수행하지 않는다.
