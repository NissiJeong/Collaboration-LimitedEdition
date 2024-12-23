# 🏕 Collaboration Limited Edition 프로젝트
## 프로젝트 설명
### 개요
- 캠핑 브랜드와 패션 브랜드의 콜라보 한정판 상품 판매/구매 서비스
- 사용자는 브랜드에서 올린 콜라보 한정판 상품을 정해진 시간에 정해진 수량 만큼 선착순으로 구매

<details>
<summary><span >프로젝트 실행 방법</span></summary>
<div markdown="1">
<ul>
<li>도커 실행 명령어</li>
<li>222</li>
</ul>
</div>
</details>

### 프로젝트 일정 계획
|12.14(토)|12.15(일)|12.16(월)|12.16(화)|
|:---:|:---:|:---:|:---:|
|프로젝트 기회<br>도커,스프링 기본세팅<br>기본구현 완료| |테스트 코드 작성<br>Optional 기능 구현|Optional 기능 구현|

### 프로젝트 환경
- Java17
- Spring Boot 3.3.0
- Gradle
- JPA
- MySQL
- Redis
- MySQL
- Redis
# 💡 프로젝트 설명 및 기술적 요구사항
### 프로젝트 설명
- 상품이 재입고 되었을 때, 재입고 알림을 설정한 유저들에게 재입고 알림
### 비즈니스 요구사항
- 재입고 알림을 전송하기 전, 상품의 재입고 회차를 1 증가
- 상품이 재입고 되었을 때, 재입고 알림을 설정한 유저들에게 알림 메시지를 전달
- 재입고 알림은 재입고 알림을 설정한 유저 순서대로 메시지 전송
- 회차별 재입고 알림을 받은 유저 목록 저장
- 재입고 알림을 보내던 중 재고가 모두 없어진다면 알림 중단
- 재입고 알림 전송의 상태를 DB 에 저장
### 기술적 요구사항
- 알림 메시지는 1초에 최대 500개의 요청
- MySQL 조회 시 인덱스를 잘 탈 수 있게 설계
- 설계해야 할 테이블 목록: Product, ProductNotificationHistory, ProductUserNotification, ProductUserNotificationHistory
- (Optional) 예외에 의해 알림 메시지 발송이 실패한 경우, manual 하게 상품 재입고 알림 메시지를 다시 보내는 API를 호출한다면 마지막으로 전송 성공한 이후 유저부터 다시 알림 메시지 전송
- (Optional) 테스트 코드 작성
## 💡 기획
### 데이터베이스 설계 및 ERD
#### 5개 테이블 관리
- Product: 상품관리
- ProductNotificationHistory: 상품별 재입고 알림 히스토리
- ProductUserNotification: 상품별 재입고 알림을 설정한 유저
- ProductUserNotificationHistory: 상품 + 유저별 알림 히스토리
- User: 사용자 관리<br>
#### [ERD]
![img.png](images/img.png)
### 알림 기능 구현 및 flow
(1) 상품 재입고 회차 1 증가 &rarr; (2) 재입고 알림 설정한 유저들에게 메시지 전달(Redis Streams 사용) &rarr; (3) 알림 전송 성공한 유저 저장 <br>
<br>
(2) 에서 고려 사항<br>
- 알림을 설정한 유서 순서대로 메시지 전송: 생성 일자로 ASC<br>
- 재입고 알림을 보내던 중 재고가 모두 없어진다면 알림 중단<br>
  추후 다시 재입고 회차가 증가하면 끊어진 사용자부터 알림 재발송 &rarr; 알림 발송 시마다 Redis 에 마지막 알림 발송 사용자 update &rarr; 알림 종료된 시점에 MySQL 과 동기화<br>
- 예외에 의해 알림이 실패할 경우 manual 하게 상품 재입고 알림 메시지를 다시 보내는 API를 호출한다면 끊어진 사용자부터 알림 재발송<br>
  예외 발생하기 전 Redis 와 MySQL 동기화 &rarr; API 호출 &rarr; 끊어진 사용자부터 알림 재발송<br>
<br>

(3) 에서 고려 사항<br>
  - 회차별 재입고 알림을 받은 유저 목록 저장: ProductUserNotificationHistory 테이블에 저장
<br>

# 💡 기술적 구현
## 1 초에 알림 500 개 제한
- 우선, 1초에 알림을 500 개 이상이 진행되는지 체크
- 500 개 이상의 알림이 1초 안에 보내진다면 rate limiter 활용하여 500개 제한
### 문제
- 알림 보내는 로직 500개 데이터를 로컬에서 테스트 해보니 1600ms 이상 소요
- 500개의 알림을 보낼 때마다 재고상태를 확인하다보니 아무리 Redis 에서 가져온다고 하더라도 로컬 환경에서는 1초 안으로 처리 안되는 듯
- 성능 개선이 필요
#### 수정 전 코드
```java
private void sendAlarm(Product product) {
    // 재입고 알림 설정 유저 select
    List<ProductUserNotification> alarmUsers = productUserNotificationRepository.findAllByProductOrderByIdAsc(product.getId());

    // 알림 전송 시점에 재고 수량 MySQL 에서 가져와서 Redis 에 저장
    redisRepository.saveProductStockCount(product);

    int checkIndex = 0;
    for(ProductUserNotification productUserNoti : alarmUsers) {
        // 재고 수량 체크
        int stockCount = redisRepository.findProductStockCount(product);
        // 재고 수량이 0이면 더이상 알림 보내지 않음.
        if(stockCount == 0) {
            // 품절에 의한 알림 발송 중단 상태 저장
            redisRepository.saveProductRestockStatus(productUserNoti.getProduct(), RestockAlarmStatusEnum.SOLD_OUT.getStatus());

            // 마지막으로 알림 보낸 사용자 저장
            redisRepository.saveLastNotificationUser(productUserNoti.getProduct(), productUserNoti.getUser());
            break;
        }
        // 알림 설정 유저에게 알림 send

        // 알림 내용 저장 Redis 에 productId, userId 키로 잡아서 저장
        redisRepository.saveProductUserNotificationInfo(productUserNoti.getProduct(), productUserNoti.getUser());
        // 마지막 사용자인 경우
        if(checkIndex == alarmUsers.size()-1) {
            // 알림 완료 상태 저장
            redisRepository.saveProductRestockStatus(productUserNoti.getProduct(), RestockAlarmStatusEnum.COMPLETED.getStatus());

            // 마지막으로 알림 보낸 사용자 저장
            redisRepository.saveLastNotificationUser(productUserNoti.getProduct(), productUserNoti.getUser());
        }
        checkIndex++;
    }
}
```
#### K6 를 사용한 성능 테스트
![img.png](images/img.png)
- 가상 유저수:1, 5번 반복 조건으로 테스트
- 해당 api 평균 속도는 1.86s 소요
- 로컬 환경이지만 성능 개선에 대한 고려가 필요해 보임
### 해결 과정
- 최초 데이터 조회 후 500번 반복문만 테스트해보니 425ms 소요
- 500번 반복될 때마다 Redis 에 저장하지 않고 끝난 후 데이터베이스에 저장해야 할 것 같은데, 그렇게 되면 중간에 재고상태가 sold out 이 되었을 때 해당 반복을 멈추게 하는 로직이 필요
- 과제의 특성상 비동기 방식을 고려하지 않기 때문에 멀티 쓰레드를 이용하여 재고 상태를 체크 고려 &rarr; 병렬 처리를 사용하게 되면 알림 요청의 순서를 완벽히 보장할 수 없음.
- Spring 의 ApplicationEventPublisher 기능을 활용한 재고 체크 로직 구현

#### 수정 후 코드
- 알림을 보낼 때마다 매번 재고 수량을 DB 에서 가져오지 않고 재고가 0이 되는 경우 이벤트 처리 
- Redis 에 알림 사용자 내역 저장을 매번 진행하지 않고 List 에 담아서 한번에 저장
```java
@EventListener
    public void handleStockChangeEvent(StockChangeEvent event) {
        // 재입고 알림 설정 유저 select
        List<ProductUserNotification> alarmUsers = productUserNotificationRepository.findAllByProductOrderByIdAsc(event.getProductId());
        // 알림 발송 후 한 번에 저장하기 위한 배열 변수
        List<String> userIds = new ArrayList<>();

        int checkIndex = 0;
        for(ProductUserNotification productUserNoti : alarmUsers) {
            // 재고가 0일 경우 알림을 중단하는 로직
            if (event.getNewQuantity() == 0) {
                // 알림 중단 로직 추가
                // 품절에 의한 알림 발송 중단 상태 저장
                redisRepository.saveProductRestockStatus(productUserNoti.getProduct(), RestockAlarmStatusEnum.SOLD_OUT.getStatus());

                // 마지막으로 알림 보낸 사용자 저장
                redisRepository.saveLastNotificationUser(productUserNoti.getProduct(), productUserNoti.getUser());
                break;
            } else {
                // Redis 에 저장할 값 한번에 저장하기 위한 로직
                userIds.add(String.valueOf(productUserNoti.getUser().getId()));

                // 마지막 사용자인 경우
                if(checkIndex == alarmUsers.size()-1) {
                    // 알림 완료 상태 저장
                    redisRepository.saveProductRestockStatus(productUserNoti.getProduct(), RestockAlarmStatusEnum.COMPLETED.getStatus());

                    // 마지막으로 알림 보낸 사용자 저장
                    redisRepository.saveLastNotificationUser(productUserNoti.getProduct(), productUserNoti.getUser());
                }
                checkIndex++;
            }
        }

        // 알람 사용자가 있고 보낸 사용자가 있으면 Redis 에 값 저장
        if(!alarmUsers.isEmpty() && !userIds.isEmpty()){
            System.out.println("alarmUsers.get(0).getProduct().getId() = " + alarmUsers.get(0).getProduct().getId());
            userIds.forEach(System.out::println);
            redisRepository.saveProductUserNotificationInfoList(alarmUsers.get(0).getProduct(), userIds);
        }
    }
```
#### 수정 후 K6 를 사용한 성능 테스트
![img_2.png](img_2.png)
- 가상 유저수:1, 5번 반복 조건으로 테스트
- 500개 알림을 보내는 api 평균 속도 1.04s 소요

### rate limiter 활용한 1초에 500번 알림 제한
- Resilience4j 사용한 500회 제한
#### 코드
```java
// 1초에 500번만 호출을 허용
@RateLimiter(name = "sendNotificationLimiter", fallbackMethod = "sendNotificationFallback")
public void sendAlarm(ProductUserNotification productUserNoti, int checkIndex, List<String> userIds, int lastIdx) {
    // Redis 에 저장할 값 한번에 저장하기 위한 로직
    userIds.add(String.valueOf(productUserNoti.getUser().getId()));

    // 마지막 사용자인 경우
    if(checkIndex == lastIdx) {
        // 알림 완료 상태 저장
        redisRepository.saveProductRestockStatus(productUserNoti.getProduct(), RestockAlarmStatusEnum.COMPLETED.getStatus());

        // 마지막으로 알림 보낸 사용자 저장
        redisRepository.saveLastNotificationUser(productUserNoti.getProduct(), productUserNoti.getUser());
    }
}

// Rate limit을 초과했을 때 호출되는 fallback 메서드
public void sendNotificationFallback(ProductUserNotification productUserNoti, int checkIndex, List<String> userIds, int lastIdx, Throwable throwable) {
    int retryCount = 0;
    long backoffTime = 1000; // 초기 대기 시간 1초

    while (retryCount < 3) {
        retryCount++;
        try {
            Thread.sleep(backoffTime); // 지수 백오프 적용
            sendAlarm(productUserNoti, checkIndex, userIds, lastIdx); // 원래 로직 재실행
            return; // 성공하면 메서드 종료
        } catch (Exception e) {
            System.out.println("Retry attempt " + retryCount + " failed.");
            backoffTime *= 2; // 대기 시간을 두 배로 늘림
        }
    }
    System.out.println("All retry attempts failed for user: " + productUserNoti.getUser().getId());
}
```
