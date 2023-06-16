import React, {FC, useState, ChangeEvent, useEffect} from 'react';
import {apiResource} from '../../api/Api';
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import {Tag} from "../../model/Tag";
import {TextResource} from "../../model/TextResource";
import '../../style/DocumentsPage.css'
import {message} from "antd";


const DocumentsPage: FC = () => {
    const [documents, setDocuments] = useState<TextResource[]>([]);
    const [filters, setFilters] = useState({ name: '', content: '', tags: [] as string[] });
    const navigate = useNavigate();

    const fetchDocuments = async () => {
        try {

            const data: { name: string, content: string, tags?: { name: string }[] } = {
                name: filters.name,
                content: filters.content,
            };

            if (filters.tags.length > 0 && filters.tags[0] !== '') {
                data.tags = filters.tags.map(tag => ({name: tag}));
            } else {
                data.tags = [];
            }

            const response = await apiResource.post('filter', data);
            if (response.status === 200) {
                setDocuments(response.data);
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                const {status, data} = error.response;
                if (status === 401) {
                    message.error( data || 'Nie masz uprawnień do wykonania tej akcji');
                }
            } else {
                message.error('Nie udało się pobrać zasobów');
            }
        }
    }

    const handleFilterChange = (event: ChangeEvent<HTMLInputElement>) => {
        setFilters(prevFilters => ({ ...prevFilters, [event.target.name]: event.target.value }));
    }

    const handleTagFilterChange = (event: ChangeEvent<HTMLInputElement>) => {
        const tagsArray = event.target.value.split(/\s*,\s*/);
        setFilters(prevFilters => ({ ...prevFilters, tags: tagsArray }));
    }


    useEffect(() => {
        (async () => {
            try {
                const response = await apiResource.get('list');

                if (response.status === 200) {
                    setDocuments(response.data);
                }
            } catch (error) {
                if (axios.isAxiosError(error) && error.response) {
                    const {status} = error.response;
                    if (status === 401) {
                        message.error('Nie jesteś zalogowany');
                    }
                }
            }
        })();
    }, [navigate]);


    return (
        <div className="documents-page">
            <div className="filters">
                <h1>Twoje dokumenty</h1>
                <input type="text" name="name" placeholder="Filtruj po nazwie" onChange={handleFilterChange} />
                <input type="text" name="content" placeholder="Filtruj po zawartości" onChange={handleFilterChange} />
                <input type="text" name="tags" placeholder="Filtruj po tagach" onChange={handleTagFilterChange} />
                <button onClick={fetchDocuments}>Filtruj</button>
            </div>
            <div className="document-list">
                {documents.map((txtres: TextResource) => (
                    <div className="document-item">
                        <div className="document-item-content">
                            <h2>
                                <Link to={`/resource/${txtres.id}`}>{txtres.name}</Link>
                            </h2>
                            <ul className="tag-list">
                                {txtres.tags.map((tag: Tag) => (
                                    <li key={tag.name}>{tag.name}</li>
                                ))}
                            </ul>
                        </div>
                        {txtres.imagePath && <img src={`http://localhost:8080${txtres.imagePath}`} alt="Obraz" />}
                    </div>

                ))}
            </div>
            <Link to={'/create'} className="create-button">Utwórz nowy dokument</Link>
        </div>
    );
};

export default DocumentsPage;