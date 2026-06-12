# PHP LSP — PhpStorm Plugin

A PhpStorm plugin that integrates [php-lsp](https://github.com/jorgsowa/php-lsp), a blazing-fast PHP language server written in Rust, via [LSP4IJ](https://github.com/redhat-developer/lsp4ij).

## Features

- **Completions** — near-instant suggestions powered by a native Rust engine
- **Diagnostics** — real-time error and warning reporting
- **Hover** — inline documentation on demand
- **Go-to-definition** — navigate your codebase instantly
- **Low memory footprint** — no JVM overhead, just Rust

## Requirements

- PhpStorm 2024.3+
- [LSP4IJ](https://plugins.jetbrains.com/plugin/23257-lsp4ij) plugin installed

## Installation

1. Install **LSP4IJ** from the JetBrains Marketplace
2. Install this plugin from the JetBrains Marketplace
3. Open a PHP project — the language server starts automatically

The `php-lsp` binary (v0.10.0) is bundled for macOS, Linux, and Windows — no separate installation needed.

## Publishing

```sh
PUBLISH_TOKEN=<your-token> JAVA_HOME=/path/to/jdk21 ./gradlew publishPlugin
```

## License

MIT
