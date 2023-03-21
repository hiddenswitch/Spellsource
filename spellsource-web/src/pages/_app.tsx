import {AppProps} from "next/app";
import "../components/global.scss"

export default ({Component, pageProps}: AppProps) => <Component {...pageProps} />;