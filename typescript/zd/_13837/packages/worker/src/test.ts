async function sleep(ms: number, label: string) {
    console.log('start', label)

    return new Promise((resolve) => setTimeout(resolve, ms));
}

(async () => {
    await Promise.all(['1', '2'].map(async (i) => {

        await Promise.all(['a', 'b', 'c'].map(async (label) => {
            await sleep(Math.random() * 2000, `local activity ${i}-${label}+1`)

            await sleep(Math.random() * 1000, `activity ${i}-${label}+1`)
        }))

        await sleep(1, `${i}-final`)
    }))

})()