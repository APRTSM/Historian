cd tokenizer-input

zip -r src-1.zip src-1
zip -r src-2.zip src-2

cd ../SourcererCC/tokenizers/file-level

python tokenizer.py zip
cat files_tokens/* > blocks.file
cp blocks.file ../../clone-detector/input/dataset/
./cleanup.sh

cd ../../../tokenizer-input

rm -rf src-1.zip src-2.zip

cd ../SourcererCC/clone-detector

python controller.py

cd ..

cat clone-detector/NODE_*/output10.0/query_* > ../results.pairs

cd clone-detector

./cleanup.sh