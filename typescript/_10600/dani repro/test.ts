process.on("unhandledRejection", (reason, promise) => {
    promise.catch((err) => { })
});


async function workflow() {
    const promises = [new Promise((resolve, reject) => { reject('error 1') }), new Promise((resolve, reject) => { reject('error 2') })]

    const resolvedPromises = await Promise.allSettled(promises)

    for (const promise of resolvedPromises) {
        if (promise.status === 'rejected') {
            throw new Error('Error')
        }
    }

}



(async () => {
    try {
        await workflow()
    } catch (error) {
        console.log('catch')
    }

    console.log('done 2')

    setTimeout(() => {

        console.log('done 3')
    }, 1000)
})()