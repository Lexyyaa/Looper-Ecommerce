import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const listProductsTrend = new Trend('list_products_req_duration');

export const options = {
    stages: [
        { duration: '10s', target: 500 },
        { duration: '20s', target: 1000 },
        { duration: '10s', target: 0 }
    ],

    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<3000'],
        'list_products_req_duration': ['p(95)<3500'],
        'http_reqs': ['count>1000'],
        'http_req_waiting': ['p(99)<4000'],
    },

    summaryTrendStats: ['avg', 'p(90)', 'p(95)', 'p(99)', 'max']
};

export default function () {
    const page = Math.floor(Math.random() * 50);
    const size = [10, 20][Math.floor(Math.random() * 2)];
    const brandId = Math.floor(Math.random() * 10000) + 1;
    const sortType = ['RECENT', 'LOW_PRICE', 'LIKE'][Math.floor(Math.random() * 3)];

    const url = `http://localhost:8080/api/v1/products?page=${page}&size=${size}&brandId=${brandId}&sortType=${sortType}`;

    const res = http.get(url);
    listProductsTrend.add(res.timings.duration);

    check(res, {
        '상품 목록 조회 성공': (r) => r.status === 200,
    });

    sleep(1);
}