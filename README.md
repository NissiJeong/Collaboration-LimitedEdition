🏕 Collaboration Limited Edition 프로젝트
====
프로젝트 소개 
----
***2024.12.18 ~ 2025.01.15***
- 캠핑 브랜드와 패션 브랜드의 콜라보 한정판 상품 판매/구매 서비스
- 사용자는 브랜드에서 올린 콜라보 한정판 상품을 정해진 시간에 준비된 재고만큼 선착순으로 구매


### ⚙️ 기술 스택
#### Backend
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> <img src="https://raw.githubusercontent.com/ydmins/YdMinS/main/icons/jwt.png" alt="jwt" height="25px"/> <img src="https://img.shields.io/badge/MSA-F05032?style=for-the-badge&logoColor=white"> <img src="https://img.shields.io/badge/spring cloud gateway-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/spring cloud eureka-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Apache Kafka-%3333333.svg?style=for-the-badge&logo=Apache Kafka&logoColor=white"> <img src="https://img.shields.io/badge/spring data JPA-007396?style=for-the-badge&logo=JPA&logoColor=white">

#### DB
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">

#### DevOps
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"> <img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">

#### Testing
<img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=JUnit5&logoColor=white"> <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=Postman&logoColor=white"> <img src="https://img.shields.io/badge/nGrinder-6DB33F?style=for-the-badge&logoColor=white"> <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white">

### 🧐 기술 스택에 대한 고민

- `MSA` 가 꼭 필요할까?
- `이벤트 기반` 아키텍처란?(feat.Kafka)
- MSA 환경에서 `도커` 구성하기
- MSA 각 서비스는 어떻게 서로를 `식별`할까?(feat.Eureka)
- `API Gateway` 도입 후 인증, 인가는 어디서?
- `Redis` 의 다양한 역할
- `JPA` 어디까지 깊어지나

### 🩻 시스템 아키텍처
![img.png](images/systemwhite.png)

주요 기능 및 기술적 구현
---

> ### Test Environment
> - **CPU:** 2.0 GHz Quad-Core Intel Core i5
> - **RAM:** 16 GB 3733 MHz LPDDR4X
> - Network: Localhost environment
### 📦 1분 동안 27만 번 조회, 막힘 없는 정확한 재고 조회
>요청사항: 힙 브랜드와의 콜라보 체어 100 판매 이벤트로 인해 일주일 전부터 해당 상품에 대한 조회와 재고에 대한 조회 트래픽이 점점 증가하고 있다. 
점점 더 많은 트래픽이 발생될 것이 예상되며 기존의 Database 에 직접 접근하여 조회 하는 방법에는 한계가 있다. 이를 처리하기 위해 조회 성능 향상이 필요하다.
- 효율적인 재고 읽기를 위해서 Redis 를 이용한 재고 캐싱 처리 적용
  - 상품 저장 시 MySQL, Redis 동시 저장
  - 상품 재고 Read 시에 Redis 에 있는 상품 재고 정보 조회
  - 주문 결제 진입 시 Redis 재고 감소
  - 상품 구매 확정 시 MySQL 재고 감소
- 1분 동안 27만건 요청에 대해 99% 퍼센트로 1초 이내 응답(TPS: 3906)

#### 상품 재고 캐싱 적용 이전과 성능 비교
|  | Database 직접 조회 | Redis 캐싱 적용 후 | 설명                                                   |
| --- | --- |---------------|------------------------------------------------------|
| HTTP 요청 성공률 | 95% | 100%          | 캐싱 전 일부 요청 실패 → 요청에 대해 100% 성공: 대용량 트래픽에서도 안정적으로 응답  |
| 평균 응답 시간 | 10.87초 | 0.252초        | 트래픽이 한 번에 몰려 평균 응답 시간이 느렸지만 캐싱 적용 후 안정적으로 0.25 초 만에 응답 |
| 1초 이내 응답 비율 | 13% | 99%           | 응답 시간 또한 캐싱 처리 후 99% 요청에 대해 1초 이내 처리                 |
| 실패율 | 4.82% | 0%            | 안정적으로 요청을 처리함으로 요청에 대한 실패율 0%                        |
| TPS | 385 | 3906          | TPS 1000% 향상 되었음                                   |

### 🦠 동시에 8500건 주문, 결제 요청에도 안정적인 재고 관리(동시성 처리 및 상품 재고의 원자적 관리)
>요청사항: 이벤트 오픈 시간이 되면 동시에 많은 사용자가 주문 및 결제 요청을 할 것으로 예상된다. 이를 위해 아래의 프로세스 및 재고의 정합성 보장이 필요하다.
> 1. 사용자가 주문을 시작하면 30분 동안은 해당 재고를 점유 하고 있으며, 결제 완료 시에 실제 재고를 감소시킨다.
> 2. 30분 안에 결제를 완료하지 않으면 재고 점유는 취소 된다
> 3. 주문이 폭주하는 상황에서 재고의 정합성이 유지 되어야 하고, 재고가 소진되면 다른 사용자는 주문할 수 없다.
#### 1. 주문 프로세스
- Redis TTL 을 이용한 재고 점유(예약) 처리
- 예약 시에는 Redis 캐싱 되어 있는 상품의 재고 감소
- 예약 시간(30분) 안에 결제 처리가 없으면 예약 취소(TTL 만료)
- 예약 취소 시 TTL 만료 이벤트 수신하여 Redis 캐싱 재고 복구

![order.png](images/order.png)

#### 2. 동시에 여러 주문이 들어올 경우 동시성 처리: 재고의 정합성 유지 및 재고 원자적 관리