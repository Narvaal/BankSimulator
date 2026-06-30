#!/usr/bin/env bash
# Builds pipeline.zip with all dependencies for Lambda deployment.
# Run from the pipeline/ directory before `terraform apply` or manual upload.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "🔧 Installing dependencies..."
rm -rf .build
mkdir .build
pip3 install -r requirements.txt -t .build/ --quiet

echo "📋 Copying source files..."
cp *.py .build/

echo "📦 Creating pipeline.zip..."
cd .build
zip -r ../pipeline.zip . -x "*.pyc" -x "__pycache__/*" > /dev/null
cd ..
rm -rf .build

echo "✅ pipeline.zip ready ($(du -sh pipeline.zip | cut -f1))"
echo ""
echo "Deploy with:"
echo "  aws lambda update-function-code --function-name rarelines-pipeline --zip-file fileb://pipeline.zip"
echo "  — or run: terraform apply (if lambda.tf references the zip)"
