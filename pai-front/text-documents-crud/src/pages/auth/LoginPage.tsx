import React, {useState} from 'react';
import {ErrorMessage, Field, Form, Formik} from 'formik';
import * as Yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { apiAuth } from '../../api/Api';
import axios from "axios";
import '../../style/LoginPage.css'

const validationSchema = Yup.object().shape({
    username: Yup.string().required('Nazwa użytkownika jest wymagana'),
    password: Yup.string().required('Hasło jest wymagane'),
});

const LoginPage = () => {

    const navigate = useNavigate();
    const [resetMessage, setResetMessage] = useState('');

    const handleSubmit = async (values: { username: string, password: string, submit: string }, {setErrors}: { setErrors: Function }) => {
        try {
            const response = await apiAuth.post('login', {username: values.username, password: values.password});
            if (response.status === 200) {
                localStorage.setItem('token', response.data.token);
                localStorage.setItem('username', values.username);
                navigate('/documents');
            } else {
                setErrors({submit: 'Wystąpił błąd podczas logowania'});
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                const {status, data} = error.response;
                if (status === 401) {
                    setErrors({submit: data.message || 'Nieprawidłowe dane logowania'});
                } else if (status === 404) {
                    setErrors({submit: data.message || 'Nie znaleziono użytkownika'});
                }
            } else {
                setErrors({submit: 'Wystąpił błąd podczas logowania'});
            }
        }
    };

    const handleReset = async (username: string) => {
        try {
            if (username) {
                await apiAuth.post('forgot-password', {
                    username : username
                });
                setResetMessage('Instrukcje dotyczące resetowania hasła zostały wysłane na Twój adres e-mail.');
            } else {
                setResetMessage('Proszę wpisać nazwę użytkownika przed resetowaniem hasła.');
            }
        } catch(error) {
            setResetMessage('Wystąpił błąd podczas resetowania hasła.');
        }
    };


    return (
        <Formik
            initialValues={{username: '', password: '', submit: ''}}
            validationSchema={validationSchema}
            onSubmit={handleSubmit}
        >
            {({values, errors}) => (
                <div className="login-page">
                    <Form>
                        <label>
                            Nazwa użytkownika:
                            <Field type="text" name="username" className="form-field"/>
                            <ErrorMessage name="username" component="div" className="error"/>
                        </label>
                        <label>
                            Hasło:
                            <Field type="password" name="password" className="form-field"/>
                            <ErrorMessage name="password" component="div" className="error"/>
                        </label>
                        {errors.submit && <div className="error">{errors.submit}</div>}
                        <button type="submit">Zaloguj</button>
                    </Form>
                    <div className="password-reset">
                        <button type="button" onClick={() => handleReset(values.username)}>Zapomniałeś hasła?</button>
                        {resetMessage && <div>{resetMessage}</div>}
                    </div>
                </div>
            )}
        </Formik>
    );
};

export default LoginPage;
