import React, { FC } from 'react';
import '../../style/ErrorPage.css';

const Error500Page: FC = () => {
    return (
        <div className="error-page">
            <h1 className="error-title">Error 500</h1>
            <p className="error-message">Coś poszło nie tak :(</p>
        </div>
    );
};

export default Error500Page;