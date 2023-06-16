import React, { FC, useEffect, useState } from 'react';
import { useParams, useNavigate } from "react-router-dom";
import {Formik, Field, Form, ErrorMessage, FormikHelpers} from 'formik';
import * as Yup from 'yup';
import {apiResource} from '../../api/Api';
import axios from "axios";
import '../../style/EditDocumentPage.css';
import Modal from 'react-modal';
import {TextResource} from "../../model/TextResource";
import '../../style/Modal.css'
import {message} from "antd";

Modal.setAppElement('#root')

interface FormValues {
    name: string,
    content: string,
    tags: string
}

const EditDocumentPage: FC = () => {
    const [document, setDocument] = useState<TextResource | null>(null);
    const { id } = useParams();
    const navigate = useNavigate();
    const [deleteModalIsOpen, setDeleteModalIsOpen] = useState(false);
    const [saveModalIsOpen, setSaveModalIsOpen] = useState(false);
    const [formValues, setFormValues] = useState<FormValues | null>(null);

    const handleOpenDeleteModal = () => {
        setDeleteModalIsOpen(true);
    };

    const handleCloseDeleteModal = () => {
        setDeleteModalIsOpen(false);
    };

    const handleOpenSaveModal = (values: FormValues) => {
        setFormValues(values);
        setSaveModalIsOpen(true);
    };

    const handleCloseSaveModal = () => {
        setSaveModalIsOpen(false);
    };

    const handleDelete = async () => {
        handleCloseDeleteModal();
        try {
            const response = await apiResource.delete(`${id}`);
            if (response.status === 200) {
                navigate('/documents');
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

    const handleSave = async () => {
        handleCloseSaveModal();
        if (!formValues) return;
        try {
            const response = await apiResource.put(`${id}`, {
                name: formValues.name,
                content: formValues.content,
                tags: formValues.tags.split(/\s*,\s*/).map(tag => ({ name: tag })),
            });
            if (response.status === 200) {
                navigate('/documents');
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

    useEffect(() => {
        (async () => {
            try {
                const response = await apiResource.get(`${id}`);
                if (response.status === 200) {
                    setDocument(response.data);
                } else {
                    navigate('/404');
                }
            } catch (error) {
                if (axios.isAxiosError(error) && error.response) {
                    const { status } = error.response;
                    if (status === 404) {
                        navigate('/404');
                    } else {
                        navigate('/500');
                    }
                } else {
                    navigate('/500');
                }
            }
        })();
    }, [id, navigate]);

    const validationSchema = Yup.object().shape({
        name: Yup.string().required('Required'),
        content: Yup.string().required('Required'),
        tags: Yup.string().required('Required'),
    });

    if (!document) {
        return <div>Loading...</div>;
    }

    return (
        <div className="edit-document-page">
            <h1>Edytuj zasób</h1>
            <Formik
                initialValues={{
                    name: document.name,
                    content: document.content,
                    tags: document.tags.map(tag => tag.name).join(', ')
                }}
                validationSchema={validationSchema}
                onSubmit={(values, { setSubmitting }: FormikHelpers<FormValues>) => {
                    setSubmitting(false);
                    handleOpenSaveModal(values);
                }}
            >
                {({ isSubmitting }) => (
                    <Form>
                        <div>
                            <label htmlFor="name">Name: </label>
                            <Field type="text" name="name" />
                            <ErrorMessage name="name" component="div" />
                        </div>
                        <div>
                            <label htmlFor="content">Content: </label>
                            <Field as="textarea" name="content" />
                            <ErrorMessage name="content" component="div" />
                        </div>
                        <div>
                            <label htmlFor="tags">Tags: </label>
                            <Field type="text" name="tags" />
                            <ErrorMessage name="tags" component="div" />
                        </div>
                        <button type="submit" disabled={isSubmitting}>Zapis</button>
                        <button type="button" onClick={handleOpenDeleteModal}>Usuń</button>
                    </Form>
                )}
            </Formik>
            <Modal
                isOpen={deleteModalIsOpen}
                onRequestClose={handleCloseDeleteModal}
                contentLabel="Confirm Delete"
                className="modal-content"
                overlayClassName="modal-overlay"
            >
                <h2>Potwierdzenie</h2>
                <p>Czy na pewno chcesz usunąć ten dokument?</p>
                <button onClick={handleDelete}>Tak, usuń</button>
                <button onClick={handleCloseDeleteModal}>Nie, anuluj</button>
            </Modal>
            <Modal
                isOpen={saveModalIsOpen}
                onRequestClose={handleCloseSaveModal}
                contentLabel="Confirm Save"
                className="modal-content"
                overlayClassName="modal-overlay"
            >
                <h2>Potwierdzenie</h2>
                <p>Czy na pewno chcesz zapisać zasób?</p>
                <button onClick={handleSave}>Tak, zapisz</button>
                <button onClick={handleCloseSaveModal}>Nie, anuluj</button>
            </Modal>
        </div>
    );
};

export default EditDocumentPage;
