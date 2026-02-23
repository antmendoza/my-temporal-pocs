```

 error: RangeError: "length" is outside of 
buffer bounds

     at Buffer.proto.utf8Write (node:internal/buffer:1066:13)

     at Op.writeStringBuffer [as fn] (/Users/${userDir}/code/${project}/node_modules/protobufjs/src/writer_buffer.js:61:13)

     at BufferWriter.finish (/Users/${userDir}/code/${project}/node_modules/protobufjs/src/writer.js:453:14)

     at WorkflowCodecRunner.encodeCompletion (/Users/${userDir}/code/${project}/node_modules/@temporalio/worker/src/workflow-codec-runner.ts:327:104)

     at async Worker.handleActivation (/Users/${userDir}/code/${project}/node_modules/@temporalio/worker/src/worker.ts:1118:28) {

   code: 'ERR_BUFFER_OUT_OF_BOUNDS'

 },

```



