import React, {FC} from 'react';
import {useNavigate, useSearchParams} from "react-router-dom";
import {apiAuth} from "../../api/Api";
import axios from "axios";
import '../../style/ResetPasswordPage.css'
import {ErrorMessage, Field, Form, Formik} from "formik";
import * as Yup from "yup";

const validationSchema = Yup.object().shape({
    password: Yup.string().required('Nazwa użytkownika jest wymagana'),
    confirmPassword: Yup.string().required('Hasło jest wymagane'),
});

const ResetPasswordPage : FC = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");

    const navigate = useNavigate();

    const handleResetPassword  = async (values: { password: string, confirmPassword: string, submit: string }, {setErrors}: { setErrors: Function }) => {

        try {
            const response = await apiAuth.post("reset-password", {
                token: token,
                password: values.password
            });
            if (response.status === 200) {
                setErrors({submit: "Hasło zostało zresetowane"});
                navigate("/login")
            } else {
                setErrors("Wystąpił błąd podczas resetowania hasła");
            }
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                const {status, data} = error.response;
                if (status === 404) {
                    setErrors({submit: data || "Nie znaleziono użytkownika o podanym tokenie"});
                } else {
                    setErrors({submit: data || "Wystąpił błąd podczas resetowania hasła"});
                }
            } else {
                setErrors({submit: "Wystąpił błąd podczas resetowania hasła"});
            }
        }
    }

    return (
        <Formik
            initialValues={{password: '', confirmPassword: '', submit: ''}}
            validationSchema={validationSchema}
            onSubmit={handleResetPassword}
        >
            {({errors}) => (
                <div className="reset-password-page">
                    <Form>
                        <label>
                            Hasło:
                            <Field type="password" name="password" className="form-field"/>
                            <ErrorMessage name="password" component="div" className="error"/>
                        </label>
                        <label>
                            Powtórz hasło
                            <Field type="password" name="confirmPassword" className="form-field"/>
                            <ErrorMessage name="confirmPassword" component="div" className="error"/>
                        </label>
                        {errors.submit && <div className="error">{errors.submit}</div>}
                        <button type="submit">Ustaw nowe hasło</button>
                    </Form>
                </div>
            )}
        </Formik>
    );
};

export default ResetPasswordPage;
