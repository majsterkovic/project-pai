import React from 'react';
import { Formik, Field, Form, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import {apiResource} from '../../api/Api';
import { useNavigate } from 'react-router-dom';
import '../../style/CreateResourcePage.css';
import { message } from 'antd';
import axios from "axios";


interface Tag {
  name: string;
}


const CreateResourcePage = () => {
  const navigate = useNavigate();

  const validationSchema = Yup.object().shape({
    name: Yup.string().required('Podaj nazwę'),
    content: Yup.string().required('Dodaj zawartość'),
    tags: Yup.string().required('Dodaj tagi')
  });

    const handleSubmit = async (values: { name: string; content: string; tags: string; file: File | null }, { setSubmitting }: any) => {
        const resource = {
            name: values.name,
            content: values.content,
            tags: values.tags.split(/\s*,\s*/).map(tag => ({ name: tag } as Tag))
        };

        const formData = new FormData();
        if (values.file) {
            const fileExtension = values.file.name.split('.').pop()?.toLowerCase();
            if (fileExtension === 'jpg' || fileExtension === 'jpeg') {
                formData.append('file', values.file);
            } else {
                message.error('Plik musi być w formacie jpg lub jpeg');
                return;
            }
        } else {
            message.error('Dodaj obrazek');
            return;
        }

        const json = JSON.stringify(resource);
        const blob = new Blob([json], {
            type: 'application/json'
        });

        formData.append('resource', blob);

        console.log('Content-Type:', formData.get('Content-Type'));
        try {
            await apiResource.post('create', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setSubmitting(false);
            navigate('/documents');
        } catch (error) {
            setSubmitting(false);
            if (axios.isAxiosError(error) && error.response) {
                const {status, data} = error.response;
                if (status === 401) {
                    message.error( data || 'Nie masz uprawnień do wykonania tej akcji');
                } else if (status === 409) {
                    message.error(data || 'Zasób o podanej nazwie już istnieje');
                }
            } else {
                message.error('Wystąpił błąd podczas tworzenia zasobu');
            }
        }
    };


  return (
      <div className="create-resource-page">
        <Formik
            initialValues={{
              name: '',
              content: '',
              tags: '',
              file: null,
            } as {
              name: string;
              content: string;
              tags: string;
              file: File | null;
            }}
            validationSchema={validationSchema}
            onSubmit={handleSubmit}
        >
          {({ isSubmitting, setFieldValue }) => (
              <Form>
                <div>
                  <label htmlFor="name">Nazwa</label>
                  <Field type="text" name="name" />
                  <ErrorMessage name="name" component="div" />
                </div>

                <div>
                  <label htmlFor="content">Zawartość</label>
                  <Field as="textarea" name="content" />
                  <ErrorMessage name="content" component="div" />
                </div>

                <div>
                  <label htmlFor="tags">Tagi</label>
                  <Field type="text" name="tags" />
                  <ErrorMessage name="tags" component="div" />
                </div>

                <div>
                  <label htmlFor="file">Obrazek</label>
                  <input id="file" name="file" type="file" onChange={(event) => {
                    if (event.currentTarget.files) {
                      setFieldValue("file", event.currentTarget.files[0]);
                    }
                  }} />

                  <ErrorMessage name="file" component="div" />
                </div>

                <button type="submit" disabled={isSubmitting}>
                  Utwórz
                </button>
              </Form>
          )}
        </Formik>
      </div>
  );
};

export default CreateResourcePage;
