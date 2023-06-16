import axios from 'axios';
import {message} from "antd";


const apiAuth = axios.create(
    {
        baseURL: 'http://localhost:8080/api/auth/'
    }
)

apiAuth.interceptors.request.use(
    (config) => {
        return config;
    }
);

apiAuth.interceptors.response.use(
    (response) => {
        return response;
    }
);

const apiResource = axios.create(
    {
        baseURL: 'http://localhost:8080/api/resource/'
    }
)

apiResource.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        config.headers.Authorization = token ? `Bearer ${token}` : ''
        return config;
    }
);

apiResource.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response) {
            if (error.response.status === 500) {
                console.log(error.response.data)
                window.location.href = '/500';
                return new Promise(() => {});
            } else if (error.response.status === 404) {
                window.location.href = '/404';
                return new Promise(() => {});
            } else if (error.response.status === 401 && error.response.data === 'TokenExpired') {
                message.info('Twoja sesja wygasła, zaloguj się ponownie').then(() => {
                    localStorage.removeItem('token');
                });
            } else {
                return Promise.reject(error);
            }
        }
    }
);


export { apiAuth, apiResource };