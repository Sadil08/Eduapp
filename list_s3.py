import boto3
import os
from dotenv import load_dotenv

load_dotenv('.env')

s3 = boto3.client('s3', 
    endpoint_url=os.environ.get('SUPABASE_S3_ENDPOINT'),
    aws_access_key_id=os.environ.get('SUPABASE_S3_ACCESS_KEY'),
    aws_secret_access_key=os.environ.get('SUPABASE_S3_SECRET_KEY'),
    region_name='auto')

try:
    print("Listing bucket eduapp-images...")
    response = s3.list_objects_v2(Bucket='eduapp-images')
    if 'Contents' in response:
        for obj in response['Contents']:
            print(f"Found key: {obj['Key']}")
    else:
        print('Bucket is empty')
except Exception as e:
    print(f'Error: {e}')
