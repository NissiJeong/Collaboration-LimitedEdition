import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 옵션 설정
export let options = {
    scenarios: {
        unique_users: {
            executor: 'per-vu-iterations', // 각 유저가 정해진 횟수만큼만 실행
            vus: 1300,                     // 1000명의 사용자
            iterations: 1,                 // 각 사용자가 1번씩만 호출
            maxDuration: '1m',             // 최대 테스트 시간
        },
    },
    thresholds: {
        http_req_failed: ['rate<1'],      // 실패율 1% 미만
        http_req_duration: ['p(95)<1000'] // 95%의 요청이 1초 미만이어야 함
    },
};

// 주문 API 요청 본문
const orderRequestBody = JSON.stringify({
    orderProductDtoList : [
        {
            productId: 1,
            orderQuantity: 10,
        }
    ],
    addressId: 1,
});

// 주문 및 결제 API 엔드포인트
const ORDER_API_URL = 'http://localhost:8083/api/order';
const PAYMENT_GET_API_URL = 'http://localhost:8084/api/payment';
const PAYMENT_API_URL = 'http://localhost:8084/api/payment';

// 임시 accessToken
const accessToken = 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJqbmlzc2k5MkBnbWFpbC5jb20iLCJhdXRoIjoiVVNFUiIsImV4cCI6MTc0NDA5MTk5NywiaWF0IjoxNzM2MzE1OTk3fQ.ZBPjhmvcB8bCyLxmXg9MVtzUaiqMtQPbo7TRgbjv-As';

export default function () {
    const user = `${__VU}`
    console.log("user id: ",user);
    // 공통 헤더 설정
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': accessToken,
        "X-Claim-sub" : user
    };

    // 1. 주문 API 호출
    let orderRes = http.post(ORDER_API_URL, orderRequestBody, { headers });

    // 주문 요청 체크
    check(orderRes, {
        'Order status is 200': (r) => r.status === 200,
        'Order response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    // 2. 주문 후 30초 이내에 결제 API 호출
    sleep(5);

    let orderResponseBody = JSON.parse(orderRes.body);
    // 3. orderId 추출
    let orderId = orderResponseBody.data.orderId;

    let res = http.get(PAYMENT_GET_API_URL+"/"+orderId);

    let paymentId = res.body;

    let paymentRes = http.put(PAYMENT_API_URL+"/"+paymentId, null, { headers });

    // 결제 요청 체크
    check(paymentRes, {
        'Payment status is 200': (r) => r.status === 200,
        'Payment response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    // 1초 대기 후 다음 사용자로 넘어감
    sleep(1);
}
