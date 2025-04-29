#!/bin/bash
export AWS_ACCESS_KEY_ID=test_access_key AWS_SECRET_ACCESS_KEY=test_secret_access_key
awslocal s3api create-bucket --bucket resources-bucket
awslocal s3api create-bucket --bucket resources-bucket-stage
awslocal s3api create-bucket --bucket fallback-bucket