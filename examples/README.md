## 🧪 Examples

Welcome! This directory contains a small, self-contained collection of runnable BELLA demos. Each example includes a BEAST XML configuration, a JSON file with `-DF` substitutions, and any required predictor and data files, allowing you to reproduce a minimal analysis from start to finish. Each example also includes a `README.md` file with additional details, explaining how to adapt the configuration and inputs for your own analyses. 📦

## ▶️ Running an example

To run the examples, you need **BEAST** and **BELLA** installed, along with the following additional BEAST packages:

- [BDMM-Prime](https://tgvaughan.github.io/BDMM-Prime/)  
- [Feast](https://tgvaughan.github.io/feast/)

Once installed, run each example with:

```bash
beast -DF <example>/data.json <example>/config.xml
```

For instance, to run the FBD example, use the following command:
```bash
beast -DF FBD/data.json FBD/config.xml
```

## 📚 Available examples

| Examples  | Description: |
|-------|---------------|
| `FBD` | Fossilized Birth-Death (FBD) analysis using BELLA to estimate skyline speciation and extinction rates for a fixed phylogenetic tree. |