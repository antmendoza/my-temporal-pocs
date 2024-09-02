import axios from "axios";

export interface Endpoint {
    get(key: string): Promise<string>;
}

export interface Data {
    field1: string
}

export const createActivities = (endpoint: Endpoint) => ({
    async myActivity(msg: string): Promise<string> {
        const name = await endpoint.get('name');
        return `${msg}: ${name}`;
    },

    async myAxiosActivity(): Promise<Data> {

        const res = await axios.get('http://httpbin.org/get?answer=42');

        return res.data;

    },
});

