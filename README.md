# Qurve Backend

> Qurve 프로젝트 백엔드 서버

---

## 🛠 기술 스택

| 항목 | 버전 |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Build Tool | Gradle - Groovy |
| Database | MySQL |
| ORM | Spring Data JPA |
| Security | Spring Security, OAuth2 Client |

---

## 📁 프로젝트 구조

```
src
└── main
    ├── java
    │   └── com.qurve
    │       ├── domain                # 도메인별 패키지 (member, level 등)
    │       └── global                # 애플리케이션 전역 공통 관리
    │           ├── config            # 애플리케이션 설정
    │           │   ├── auth          # JWT 관련 설정
    │           │   ├── filter        # 요청 로깅 등 필터 설정
    │           │   └── swagger       # Swagger 설정
    │           ├── dto               # 공통 Response 객체
    │           ├── entity            # 생성/수정 시간 자동 기록 BaseEntity
    │           ├── enums             # 공통 에러 코드 및 Enum 관리
    │           ├── exception         # 전역 예외 처리
    │           ├── pagination        # 페이징 관련 설정 및 DTO
    │           └── security          # Spring Security 설정 관리
    └── resources
        └── application.yml           # 공통 설정
.env                                  # 환경변수 (git 제외, 루트에 위치)
```

---

## ⚙️ 로컬 환경 세팅

### 1. 레포 클론

```bash
git clone https://github.com/Quad-team05/qurve-backend.git
cd qurve-backend
```

### 2. 환경변수 설정

프로젝트 루트에 `.env` 파일 생성 후 아래 값 입력

```
DB_URL=jdbc:mysql://localhost:3306/qurve
DB_USERNAME=본인DB유저명
DB_PASSWORD=본인DB비밀번호

JWT_SECRET=32자리이상아무문자열
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

### 3. MySQL DB 생성

```sql
CREATE DATABASE qurve;
```

### 4. 실행

IntelliJ 실행
