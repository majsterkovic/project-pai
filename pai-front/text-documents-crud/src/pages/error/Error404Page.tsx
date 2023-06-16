import React, { FC } from 'react';
import '../../style/ErrorPage.css';

const Error404Page: FC = () => {
    return (
        <div className="error-page">
            <h1 className="error-title">Error 404</h1>
            <p className="error-message">Nie odnaleziono :(</p>
        </div>
    );
};


export default Error404Page;
