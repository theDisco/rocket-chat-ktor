import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// Define a custom metric to track response times
let responseTimeTrend = new Trend('response_time');
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export let options = {
    stages: [
        { duration: '300s', target: 10 }, // Ramp up to 10 users over 30 seconds
        { duration: '900s', target: 10 }, // Stay at 10 users for 90 seconds
        { duration: '300s', target: 0 },  // Ramp down to 0 users
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'],  // 95% of requests should be under 500ms
    },
};

export default function () {
    group('User CRUD Operations', function () {
        // For 10% of the requests, cause a server error
        if (Math.random() < 0.1) {
            const userPayloadWithError = JSON.stringify({
                name: 'Test User',
                email: `testuser_${Math.floor(Math.random() * 100000)}@example.com`,
                foo: 'bar', // This will cause a 500 error
            });
            http.post(`${BASE_URL}/users`, userPayloadWithError, {
                headers: { 'Content-Type': 'application/json' },
            })
        }

        const userPayload = JSON.stringify({
            name: 'Test User',
            email: `testuser_${Math.floor(Math.random() * 100000)}@example.com`,
        });

        // Create a user
        let res = http.post(`${BASE_URL}/users`, userPayload, {
            headers: { 'Content-Type': 'application/json' },
        });
        check(res, {
            'User created': (r) => r.status === 201,
        });
        responseTimeTrend.add(res.timings.duration);

        const userId = res.json().id;
        const randomReads = Math.floor(Math.random() * 10) + 1;

        for (let i = 0; i < randomReads; i++) {
            res = http.get(`${BASE_URL}/users/${userId}`);
            check(res, { 'User fetched': (r) => r.status === 200 });
            responseTimeTrend.add(res.timings.duration);
            sleep(0.5);
        }

        res = http.get(`${BASE_URL}/users`);
        check(res, { 'User list fetched': (r) => r.status === 200 });
        responseTimeTrend.add(res.timings.duration);

        if (Math.random() < 0.4) {
            const updatePayload = JSON.stringify({name: 'Updated User'});
            res = http.put(`${BASE_URL}/users/${userId}`, updatePayload, {
                headers: {'Content-Type': 'application/json'},
            });
            check(res, {
                'User updated': (r) => r.status === 200,
            });
            responseTimeTrend.add(res.timings.duration);
        }

        if (Math.random() < 0.1) {
            res = http.del(`${BASE_URL}/users/${userId}`);
            check(res, {
                'User deleted': (r) => r.status === 204,
            });
            responseTimeTrend.add(res.timings.duration);
        }

        sleep(1);
    });
}