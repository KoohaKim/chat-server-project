import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '30s', target: 300 },
        { duration: '30s', target: 500 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<400'],
    },
};

const BASE_URL = 'http://localhost:8080';
const USER_IDS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
const TEST_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJLaW0iLCJ1c2VySWQiOjEsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzgwOTIyODA2LCJleHAiOjQ5MzQ1MjI4MDZ9.o7Tr1eZD8fMAJqpE1yA8YaWc2t4W7lfFjZgOk5KHxxI';

export default function () {
    const randomUserId = USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
    const url = `${BASE_URL}/api/chatRooms/MyChatRooms/${randomUserId}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${TEST_TOKEN}`,
        },
    };

    const response = http.get(url, params);

    check(response, {
        'is status 200': (r) => r.status === 200,
        'body is not null': (r) => r.body !== null,
    });

    sleep(Math.random() * 0.4 + 0.3);
}