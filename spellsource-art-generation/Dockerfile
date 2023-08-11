FROM python:3.11

ADD . /spellsource-art-generation

RUN python -m pip install -U pip && \
    pip install ./spellsource-art-generation && \
    if [ "$(uname -m)" = "x86_64" ]; then \
        pip uninstall --yes torch torchvision torchaudio && \
        pip install -U --pre torch torchvision torchaudio --index-url https://download.pytorch.org/whl/nightly/cu118; \
    fi \
    rm -rf ./spellsource-art-generation

EXPOSE 8188

ENTRYPOINT ["comfyui"]