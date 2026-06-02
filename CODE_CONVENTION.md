# Qurve 백엔드 코드 컨벤션

## 프로젝트 구조

```
com.qurve
├── domain
│   └── {도메인}
│       ├── controller
│       ├── service
│       ├── repository
│       ├── domain
│       ├── dto
│       │   ├── request
│       │   └── response
│       └── enums
└── global
    ├── config
    ├── dto
    ├── entity
    ├── enums
    ├── exception
    ├── pagination
    └── security
```

### 폴더 역할

| 폴더 | 설명 |
| --- | --- |
| controller | HTTP 요청/응답 처리 |
| service | 비즈니스 로직 |
| repository | DB 접근 |
| domain | 엔티티 |
| dto | 요청/응답 객체 |
| enums | Enum 관리 |

---

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스 | PascalCase | `UserService` |
| 메서드 | camelCase | `findUser` |
| 변수 | camelCase | `accessToken` |
| 상수 | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| 패키지 | 의미기반, 소문자, 언더스코어 금지 | `com.qurve.user.controller` |
| DB 컬럼 | snake_case | `user_id` |

---

## 메서드 규칙

### 메서드 네이밍

| 종류 | 설명 | 이름 규칙 |
| --- | --- | --- |
| Create | 생성 | `save` |
| Update | 수정 | `update` / `update + Column` (예: `updatePassword`) |
| Delete | 삭제 | `delete` |
| 단일 조회 | 단건 조회 | `findOne` / `findOneByEmail` |
| 리스트 조회 | 다건 조회 | `findAll` / `findAllByAdmin` |

### 메서드 구조

- 비슷한 기능의 메서드는 가까운 위치에 배치
- 접근 지시자, 매개변수, 반환 타입, 주석 명시

```java
/**
 * 유저를 저장합니다.   ← 주석
 */
public              // ← 접근 지시자 (public / private / protected)
UserResponseDto     // ← 반환 타입
save(UserSaveRequestDto dto) {  // ← 매개변수
    // ...
}
```

| 항목 | 설명 | 예시 |
| --- | --- | --- |
| 접근 지시자 | 외부에서 접근 가능 여부 | `public`, `private` |
| 반환 타입 | 메서드가 돌려주는 값의 타입 | `UserResponseDto`, `void` |
| 매개변수 | 메서드에 넘기는 값 | `UserSaveRequestDto dto` |
| 주석 | 메서드가 하는 일 설명 | `/** 유저를 저장합니다. */` |

---

## API 규칙

### HTTP Method

| Method | 용도 | Body 여부 |
| --- | --- | --- |
| GET | 조회 | X |
| POST | 생성 / 이벤트 | O |
| PUT | 전체 수정 | O |
| PATCH | 부분 수정 | O |
| DELETE | 삭제 | X |

### URL 규칙

- 명사 사용
- ❌ `POST /api/v1/user/save`
- ✅ `POST /api/v1/user`

---

## DTO 규칙

- Request / Response 명확히 구분
  - `UserSaveRequestDto`, `UserResponseDto`
- Response DTO는 정적 팩토리 메서드 사용

```java
public static UserResponseDto from(User user) {
    return UserResponseDto.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .build();
}
```

```java
// ❌
UserResponseDto dto = new UserResponseDto(user.getId(), user.getEmail());

// ✅
UserResponseDto dto = UserResponseDto.from(user);
```

---

## 주석 규칙

- 무엇을 했는지 보다 왜 그렇게 했는지를 설명
- 코드만 봐도 쉽게 알 수 있는 내용은 주석 X
- 외부 공개 메서드 / 복잡한 로직은 Javadoc 사용

```java
// ❌
// 레벨 테스트 결과를 저장
levelTestRepository.save(result);

// ✅
// 다시 보기 시 결과를 저장하지 않으므로 임시 객체로만 반환
levelTestRepository.save(result);

/**
 * 레벨 테스트 답안 제출 및 결과 반환
 *
 * 결과는 저장하지 않으며 채점 후 즉시 반환한다.
 *
 * @param dto 답안 제출 요청 정보
 * @return 채점된 레벨 테스트 결과
 * @throws BusinessException 문제 ID가 존재하지 않는 경우
 */
public LevelTestResultResponseDto submit(LevelTestSubmitRequestDto dto) {
    // ...
}
```

---

## 레이어별 규칙

- **Controller** — DTO 입출력, 비즈니스 로직은 Service에 위임
- **Service** — 비즈니스 로직, `@Transactional` 관리, 조회는 `readOnly = true`
- **Repository** — Spring Data JPA 중심
- **Entity** — Setter 지양, 도메인 메서드 사용 (`changePassword` 등)

---

## 예외 처리

- 도메인별 커스텀 예외 정의 (`UserNotFoundException` 등)
- 전역 예외 처리 — `@RestControllerAdvice` 사용
- 요청 DTO 검증 — `@Valid` + Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email` 등)

---

## Lombok 규칙

| 허용 | 지양 |
| --- | --- |
| `@Getter` | `@Data` |
| `@RequiredArgsConstructor` | 필드 주입 (`@Autowired`) |
| `@Builder` | |
| `@Slf4j` | |

- 의존성 주입은 생성자 주입 사용 (`@RequiredArgsConstructor`)
