# SOMACOM 3-Tier Architecture 배포 가이드

이 가이드는 SOMACOM 프로젝트를 **데이터베이스**, **백엔드**, **프론트엔드** 3개의 독립된 서버로 분리하여 배포하는 절차를 설명합니다.

---

## 🗺️ 전체 아키텍처 및 IP 정보

| 서버 역할 | IP 주소 | 실행 서비스 | 포트 | 비고 |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | `3.26.244.183` | Nginx (React 정적 파일) | 80 | 사용자 접속 진입점 |
| **Backend** | `3.106.195.135` | Spring Boot (Docker) | 8080 | API 서버 |
| **Database** | `13.239.20.87` | MariaDB (Docker) | 3306 | 데이터 저장소 |

---

## 1️⃣ Server 1: Database (MariaDB)

**대상 IP**: `13.239.20.87`

### 1. AWS 보안 그룹 (Security Group) 설정
*   **인바운드 규칙 추가**:
    *   **유형**: `MySQL/Aurora` (TCP 3306)
    *   **소스**: `3.106.195.135/32` (백엔드 서버 IP만 허용하여 보안 강화)

### 2. MariaDB 컨테이너 실행
서버에 SSH 접속 후 Docker를 사용하여 MariaDB를 실행합니다.

```bash
# 1. 기존 컨테이너 정리 (필요 시)
sudo docker rm -f mariadb

# 2. MariaDB 실행
# -e MARIADB_ROOT_PASSWORD: application-prod.properties의 비밀번호와 일치해야 함 (7564)
# -e MARIADB_DATABASE: 초기 생성할 DB명 (somacom)
sudo docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=7564 \
  -e MARIADB_DATABASE=somacom \
  --restart always \
  mariadb:latest
```

---

## 2️⃣ Server 2: Backend (Spring Boot)

**대상 IP**: `3.106.195.135`

### 1. 프로젝트 설정 확인 (`application-prod.properties`)
배포 전 로컬 프로젝트의 `src/main/resources/application-prod.properties` 파일이 아래와 같이 설정되어 있는지 확인합니다.

```properties
# DB 연결 설정
spring.datasource.url=jdbc:mariadb://13.239.20.87:3306/somacom
spring.datasource.username=root
spring.datasource.password=7564

# CORS 설정 (프론트엔드 IP 허용)
cors.allowed-origins=http://3.26.244.183

# JPA 설정 (최초 배포 시 update, 이후 validate 권장)
spring.jpa.hibernate.ddl-auto=update

# 로그 최적화 (성능 이슈 방지)
spring.jpa.show-sql=false
```

### 2. AWS 보안 그룹 설정
*   **인바운드 규칙 추가**:
    *   **유형**: `사용자 지정 TCP` (TCP 8080)
    *   **소스**: `3.26.244.183/32` (프론트엔드 서버 IP 허용)

### 3. 배포 및 실행
프로젝트 코드를 서버로 업로드한 후 (`git clone` 또는 파일 업로드), 아래 명령어를 실행합니다.

```bash
# 프로젝트 폴더로 이동
cd ~/somacom

# 1. 기존 컨테이너 중지 및 삭제
sudo docker rm -f somacom-con

# 2. Docker 이미지 빌드
sudo docker build -t somacom-img .

# 3. 컨테이너 실행
# -v 옵션: 파일 업로드 경로 마운트
sudo docker run -d \
  -p 8080:8080 \
  -v /home/ubuntu/somacom/upload:/app/upload \
  --name somacom-con \
  somacom-img \
  --spring.profiles.active=prod

# 4. 로그 확인 (실시간)
sudo docker logs -f somacom-con
```

---

## 3️⃣ Server 3: Frontend (React + Nginx)

**대상 IP**: `3.26.244.183`

### 1. React 빌드 (로컬 PC)
로컬 개발 환경에서 빌드 명령어를 실행하여 배포 파일을 생성합니다.

```bash
# 프로젝트 루트에서 실행
npm run build
```
*   결과물: 프로젝트 폴더 내 `dist` (또는 `build`) 폴더 생성.
*   내용물 확인: `index.html`, `assets/`, `vite.svg` 등이 있어야 함.

### 2. AWS 보안 그룹 설정
*   **인바운드 규칙 추가**:
    *   **유형**: `HTTP` (TCP 80)
    *   **소스**: `0.0.0.0/0` (모든 사용자 접속 허용)

### 3. Nginx 설치 및 파일 업로드
서버(`3.26.244.183`)에 접속하여 진행합니다.

```bash
# 1. Nginx 설치
sudo apt update
sudo apt install nginx -y

# 2. 기존 기본 파일 삭제
sudo rm -rf /var/www/html/*

# 3. 파일 업로드 (Local -> Server)
# MobaXterm SFTP 등을 이용하여 로컬의 'dist' 폴더 안의 *모든 내용물*을
# 서버의 '/var/www/html/' 경로로 업로드합니다.
# 주의: /home/ubuntu/var/... 가 아니라 /var/www/html/ 입니다.
```

### 4. 권한 설정 (403 Forbidden 방지)
업로드 후 반드시 권한을 설정해야 합니다.

```bash
sudo chmod -R 755 /var/www/html
sudo chown -R www-data:www-data /var/www/html
```

### 5. Nginx 리버스 프록시 설정
`/etc/nginx/sites-available/default` 파일을 수정하여 API 요청을 백엔드로 전달합니다.

```bash
sudo vi /etc/nginx/sites-available/default
```

**설정 내용:**
```nginx
server {
    listen 80;
    server_name 3.26.244.183;

    root /var/www/html;
    index index.html;

    # React Router 새로고침 404 방지
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 요청을 백엔드 서버로 전달 (Reverse Proxy)
    location /api/ {
        proxy_pass http://3.106.195.135:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 6. Nginx 재시작
```bash
sudo nginx -t
sudo systemctl restart nginx
```

---

## 🚨 트러블슈팅 (자주 발생하는 문제)

### Q1. 브라우저에서 접속 시 "응답 시간이 너무 오래 걸립니다"
*   **원인**: AWS 보안 그룹에서 80번 포트가 열려있지 않음.
*   **해결**: Frontend 서버의 보안 그룹 인바운드 규칙에 `HTTP (80)` / `0.0.0.0/0` 추가.

### Q2. "403 Forbidden" 에러 발생
*   **원인 1**: 파일이 `/var/www/html/`이 아닌 `/var/www/html/dist/`에 들어감.
*   **원인 2**: 파일 권한 문제.
*   **해결**:
    ```bash
    # 파일 위치 이동
    sudo mv /var/www/html/dist/* /var/www/html/
    # 권한 재설정
    sudo chmod -R 755 /var/www/html
    ```

### Q3. "Connection refused" (포트 5173 접속 시)
*   **원인**: 배포 환경에서는 개발용 포트(5173)가 아닌 웹 표준 포트(80)를 사용함.
*   **해결**: 주소창에 포트 번호 없이 `http://3.26.244.183` 입력.

### Q4. API 호출 시 CORS 에러
*   **원인**: 백엔드 서버가 프론트엔드 오리진을 허용하지 않음.
*   **해결**: 백엔드 `application-prod.properties`의 `cors.allowed-origins` 값이 프론트엔드 IP(`http://3.26.244.183`)와 일치하는지 확인 후 재배포.

### Q5. 배포 중 EC2 멈춤 (SSH 접속 불가)
*   **원인**: 메모리/CPU 부족 (특히 빌드나 대량 로그 발생 시).
*   **해결**: AWS 콘솔에서 인스턴스 재부팅. 운영 시 `spring.jpa.show-sql=false` 설정 필수.