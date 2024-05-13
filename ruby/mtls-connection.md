
https://github.com/coinbase/temporal-ruby/blob/master/examples/init.rb


client_key = File.read("/path-to/entity.key")
client_cert = File.read("/path-to/entity.pem")

Temporal.configure do |config|
config.host = ENV.fetch('TEMPORAL_HOST', '{namespace}.tmprl.cloud')
config.port = ENV.fetch('TEMPORAL_PORT', 7233).to_i
config.namespace = ENV.fetch('TEMPORAL_NAMESPACE', '{namespace}')
config.task_queue = ENV.fetch('TEMPORAL_TASK_QUEUE', '{taskqueue}')
config.metrics_adapter = Temporal::MetricsAdapters::Log.new(metrics_logger)
config.credentials = GRPC::Core::ChannelCredentials.new(nil, client_key, client_cert)
end


client_key = File.read("/path-to/entity.key")
client_cert = File.read("/path-to/entity.pem")

Temporal.configure do |config|
config.host = ENV.fetch('TEMPORAL_HOST', '{namespace}.tmprl.cloud')
config.port = ENV.fetch('TEMPORAL_PORT', 7233).to_i
config.namespace = ENV.fetch('TEMPORAL_NAMESPACE', '{namespace}')
config.task_queue = ENV.fetch('TEMPORAL_TASK_QUEUE', '{taskqueue}')
config.metrics_adapter = Temporal::MetricsAdapters::Log.new(metrics_logger)
config.credentials = GRPC::Core::ChannelCredentials.new(nil, client_key, client_cert)
end