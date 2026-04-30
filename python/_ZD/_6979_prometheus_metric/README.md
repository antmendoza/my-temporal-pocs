
python3 -m venv path/to/venv
cd path/to/venv/bin
source activate

pip3 install prometheus_client

pip3 install temporalio


cd ../../../../


> Terminal 1

python3 worker.py 


> Terminal 2

python3 worker_9001.py 


> Terminal 3

docker compose up 
