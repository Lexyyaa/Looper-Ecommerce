import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const PRODUCT_IDS = [2313370,2313371,2313376,2313377,2313378,2313380,2313382,2313385,2313387,2313388,2313389,2313392,2313393,2313394,2313395,2313397,2313398];
const BASE_URL = 'http://localhost:8080';

const productDetailTrend = new Trend('product_detail_req_duration');

export const options = {
    stages: [
        { duration: '4s', target: 400 },
        { duration: '10s', target: 1000 },
        { duration: '6s', target: 600 },
    ],

    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<300'],
        'product_detail_req_duration': ['p(95)<300'],
    },

    summaryTrendStats: ['avg', 'p(90)', 'p(95)', 'p(99)', 'max']
};

export default function () {
    const productId = PRODUCT_IDS[randomIntBetween(0, PRODUCT_IDS.length - 1)];

    const res = http.get(`${BASE_URL}/api/v1/products/${productId}`);
    productDetailTrend.add(res.timings.duration);

    check(res, {
        '상품 상세 조회 성공 (정상 또는 예외)': (r) => r.status === 200 || r.status === 404 || r.status === 400 || r.status === 500 || r.status === 409
    });

    sleep(0.5);
}