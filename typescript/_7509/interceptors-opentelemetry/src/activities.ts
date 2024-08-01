import {ServerResponse} from "http";

// @ts-ignore
import * as mongodb from 'mongodb';
// @ts-ignore
import {Collection} from 'mongodb';



export const createActivities = (db:any) => ({


    async greet(msg: string): Promise<string> {
        console.log(db)

        await handleInsertQuery(db)
        return `${msg}`;
    },
});



const COLLECTION_NAME = 'users'



function handleInsertQuery(db:any) {
    const obj = {name: 'John', age: '20'};
    const usersCollection: Collection = db.collection(COLLECTION_NAME);
    usersCollection.insertOne(obj)
        .then(() => {
            console.log('1 document inserted');
            // find document to test context propagation using callback
            usersCollection.findOne({}, function () {
                console.log("")
            });
        })
        .catch((err: { code: any; message: any; }) => {
            console.log('Error code:', err.code);
        });
}
