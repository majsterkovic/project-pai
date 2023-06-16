import React, { FC, useEffect, useState } from 'react';
import {apiResource} from '../../api/Api';
import {useNavigate, useParams} from 'react-router-dom';
import {Link} from "react-router-dom";
import { message } from 'antd';
import '../../style/TextResourcePage.css';
import { TextResource } from '../../model/TextResource';
import axios from "axios";


const TextResourcePage: FC = () => {
    const [textResource, setTextResource] = useState<TextResource | null>(null);
    const { id, publicUrlToken } = useParams<{ id: string, publicUrlToken?: string }>();
    const isLoggedIn = !!localStorage.getItem('token');
    const navigate = useNavigate();

    useEffect(() => {
        const fetchResource = async () => {
            try {
                let response;
                if (isLoggedIn) {
                    response = await apiResource.get(`${id}`);
                } else {
                    response = await apiResource.get(`${id}/${publicUrlToken}`);
                }
                if (response.status === 200) {
                    setTextResource(response.data);
                }
            } catch (error) {
                if (axios.isAxiosError(error) && error.response) {
                    const { status, data } = error.response;
                    if (status === 401) {
                        message.error(data || 'Nie jesteś zalogowany');
                    } else if (status === 204) {
                        message.error(data || 'Nie odnaleziono zasobu');
                    }
                } else {
                    navigate('/500');
                }
            }
        };

        fetchResource().then(() => console.log('Resource fetched'));
    }, [id, publicUrlToken, isLoggedIn, navigate]);

    const generatePublicUrl = async () => {
        try {
            const response = await apiResource.get(`${id}/generate-url`);

            if (response.status === 201) {
                console.log(response.data);
                const publicUrlToken = response.data.publicUrlToken;
                const publicUrl = `${window.location.origin}/resource/${id}/${publicUrlToken}`;
                await navigator.clipboard.writeText(publicUrl);
                message.success(`Adres URL został skopiowany: ${publicUrl}`);
            } else {
                message.error('Nie udało się wygenerować adresu URL');
            }
        } catch (error) {
            message.error('Nie udało się wygenerować adresu URL');
        }
    };



    if (!textResource) {
        return (
            <div className="loading-container">
                Loading...
            </div>
        );
    }

    return (
        <div className="text-resource-page">
            <h1 className="text-resource-title">{textResource.name}</h1>
            <p className="text-resource-content">{textResource.content}</p>
            {textResource.imagePath && <img className="text-resource-image" src={`http://localhost:8080${textResource.imagePath}`} alt="Obrazek" />}
            <ul className="text-resource-tags">
                {textResource.tags.map((tag, index) => (
                    <li key={index}>{tag.name}</li>
                ))}
            </ul>
            <div className="button-container">
                {isLoggedIn && (
                    <Link to={`/edit/${textResource.id}`}>
                        <button className="edit-button">Edytuj</button>
                    </Link>
                )}
            </div>
            <div className="button-container">
                {isLoggedIn && (
                    <button className="generate-url-button" onClick={generatePublicUrl}>
                        Wygeneruj publiczny adres URL
                    </button>
                )}
            </div>

        </div>
    );
};

export default TextResourcePage;
