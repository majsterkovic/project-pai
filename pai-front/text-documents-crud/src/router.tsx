import {createBrowserRouter, Navigate} from 'react-router-dom';
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import ResetPasswordPage  from "./pages/auth/ResetPasswordPage";
import DocumentsPage from "./pages/resource/DocumentsPage";
import CreateResourcePage from "./pages/resource/CreateResourcePage";
import {ReactNode} from "react";
import EditDocumentPage from "./pages/resource/EditDocumentPage";
import TextResourcePage from "./pages/resource/TextResourcePage";
import Error500Page from "./pages/error/Error500Page";
import Error404Page from "./pages/error/Error404Page";

const Protected = ({ children }: { children: ReactNode }) => {
    const isLoggedIn = !!localStorage.getItem('token');

    if (!isLoggedIn) {
        return <Navigate to="/login" replace />;
    }
    return <>{children}</>;
};


const router = createBrowserRouter([
    {
        path: '*',
        element: <Protected><DocumentsPage/></Protected>
    },
    {
        path: '/login',
        element: <LoginPage/>
    },
    {
        path: '/register',
        element: <RegisterPage/>
    },
    {
        path: '/documents',
        element: <Protected><DocumentsPage/></Protected>
    },
    {
        path: '/create',
        element: <Protected><CreateResourcePage/></Protected>
    },
    {
        path: '/edit/:id',
        element: <Protected><EditDocumentPage/></Protected>
    },
    {
        path: '/resource/:id', // add this new route
        element: <Protected><TextResourcePage/></Protected>
    },
    {
        path: '/resource/:id/:publicUrlToken',
        element: <TextResourcePage/>
    },
    {
        path: '/500',
        element: <Error500Page/>
    },
    {
        path: '/404',
        element: <Error404Page/>
    },
    {
        path: '/reset-password/',
        element: <ResetPasswordPage/>
    },

]);


export default router;