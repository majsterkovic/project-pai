import React, {FC} from 'react';
import { useNavigate } from 'react-router-dom';
import {Formik, Field, Form, ErrorMessage} from 'formik';
import {apiAuth } from '../../api/Api';
import axios from "axios";
import * as Yup from 'yup';
import '../../style/RegisterPage.css'

const validationSchema = Yup.object().shape({
    username: Yup.string().required('Nazwa użytkownika jest wymagana'),
    password: Yup.string().required('Hasło jest wymagane'),
    passwordConfirmation: Yup.string()
        .oneOf([Yup.ref('password'), undefined], 'Hasła muszą być takie same')
        .required('Potwierdzenie hasła jest wymagane'),
    email: Yup.string()
        .email('Adres email jest niepoprawny')
        .required('Adres email jest wymagany'),
});

const RegisterPage: FC = () => {

    const navigate = useNavigate();

    const handleSubmit = async (values: { username: string, password: string, passwordConfirmation: string, email: string, submit: string }, {setErrors}: { setErrors: Function }) => {
        try {
            const response = await apiAuth.post('signup', {
                username: values.username,
                password: values.password,
                email: values.email
            });
            if (response.status === 201) {
                navigate('/login');
            } else {
                setErrors({submit: 'Wystąpił błąd podczas rejestracji'});
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                const {status, data} = error.response;
                if (status === 409) {
                    setErrors({submit: data || 'Użytkownik o podanej nazwie już istnieje'});
                } else {
                    setErrors({submit: data || 'Wystąpił błąd podczas rejestracji'});
                }
            } else {
                setErrors({submit: 'Wystąpił błąd podczas rejestracji'});
            }
        }
    };


    return (
        <Formik
            initialValues={{username: '', password: '', passwordConfirmation: '', email: '', submit: ''}}
            validationSchema={validationSchema}
            onSubmit={handleSubmit}
        >
            {({errors}) => (
                <div className="register-page">
                    <div className="info">
                        <h1>Menadżer danych tekstowych</h1>
                        <p>Aplikacja Menadżer danych tekstowych umożliwia użytkownikom zarządzanie swoimi danymi tekstowymi. Zalogowani użytkownicy mają możliwość tworzenia, odczytywania, modyfikowania i usuwania zasobów tekstowych. Ponadto, użytkownicy mogą udostępniać swoje zasoby tekstowe innym użytkownikom w trybie "tylko do odczytu".</p>

                    </div>
                    <Form>
                        <label>
                            Nazwa użytkownika:
                            <Field type="text" name="username" className="form-field"/>
                            <ErrorMessage name="username" component="div" className="error" />
                        </label>
                        <label>
                            Hasło:
                            <Field type="password" name="password" className="form-field"/>
                            <ErrorMessage name="password" component="div" className="error"/>
                        </label>
                        <label>
                            Powtórz hasło:
                            <Field type="password" name="passwordConfirmation" className="form-field"/>
                            <ErrorMessage name="passwordConfirmation" component="div" className="error" />
                        </label>
                        <label>
                            Email:
                            <Field type="email" name="email" className="form-field"/>
                            <ErrorMessage name="email" component="div" className="error" />
                        </label>
                        {errors.submit && <div className="error">{errors.submit}</div>}
                        <button type="submit">Rejestruj</button>
                    </Form>
                </div>
            )}
        </Formik>
    );
};

export default RegisterPage;
