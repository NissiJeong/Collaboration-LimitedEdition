import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '1m', target: 10000 }, // 1분 동안 10,000명까지 증가
        { duration: '10s', target: 0 },     // 10초 동안 종료
    ],
};

export default function () {
    let res = http.get('http://localhost:8082/api/product/detail/stock/1'); // 스프링 엔드포인트 주소
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
    });
    sleep(1); // 1초 대기
}