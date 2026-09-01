# Contributing to Hakunapi

Thanks for taking the time to contribute to Hakunapi! See the [Table of Contents](#table-of-contents)
for different ways to help and details about how this project handles them.

## Table of Contents

- [Level of Support](#level-of-support)
- [I Have a Question](#i-have-a-question)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Enhancements](#suggesting-enhancements)
- [Reporting Security Issues](#reporting-security-issues)
- [I Want To Make a Pull Request](#i-want-to-make-a-pull-request)
- [Community Modules](#community-modules)

## Level of Support

This project is distributed in the hope that it will be useful, but without any warranty.
However, the project is under active development at the National Land Survey of Finlad (NLS) and we 
provide limited support for it.
- We will read the bug reports and enhancement suggestions which are filed as
[Issues](https://github.com/nlsfi/hakunapi/issues) and post comments on them.
- We will review and comment the Pull Requests. Accepted Pull Requests will be merged.
- We can be contacted privately by sending email to <os@nls.fi>

The above applies to the core modules under `src/`. Community modules under
`src-community/` are maintained by their submitters; see
[Community Modules](#community-modules).

## I Have a Question

Before you ask a question, it is best to search for existing [Issues](https://github.com/nlsfi/hakunapi/issues) that
might help you. In case you have found a suitable issue and still need clarification,
you can write your question in this issue.

If you then still feel the need to ask a question you can open an [Issue](https://github.com/nlsfi/hakunapi/issues/new).
Please provide as much context as you can about what you're running into.

If you have a question that you don't want to become public, you can send it by email
to <os@nls.fi>.

## Reporting Bugs

Before you report a bug, search for existing [Issues](https://github.com/nlsfi/hakunapi/issues) and check if your problem
is already reported. If the bug is new:
- Open an [Issue](https://github.com/nlsfi/hakunapi/issues/new).
- Explain the behavior you would expect and the actual behavior.
- Please provide as much context as possible and describe the reproduction steps 
that someone else can follow to recreate the issue on their own.

## Suggesting Enhancements

Enhancement suggestions are tracked as [GitHub issues](https://github.com/nlsfi/hakunapi/issues). Check the existing
feature requests and if your idea appears to be new:
- Open an [Issue](https://github.com/nlsfi/hakunapi/issues/new).
- Provide a detailed description of the suggested enhancement.
- Explain why this enhancement would be useful to most users.

## Reporting Security Issues

You must never report security related issues, vulnerabilities or bugs including
sensitive information to the issue tracker, or elsewhere in public.
Instead sensitive bugs must be sent by email to <os@nls.fi>.

## I Want To Make a Pull Request

If you want to help the project with a code contribution, please open a Pull Request
with your proposed changes. Your Pull Request will be reviewed and a maintainer may
ask you to make changes before merging it.

> ### Legal Notice 
> When contributing to this project, you must agree that you have authored 100%
of the content, that you have the necessary rights to the content and that the
content you contribute may be provided under the project license.

## Community Modules

Hakunapi has a second tier of modules for contributions that are useful but not part of
the supported core: community modules, living in the `src-community/` folder alongside
the core `src/` folder.

Community modules are built, tested and versioned together with the core modules, so a
change that breaks them is noticed in CI. They are, however, **not bundled in any core
Hakunapi artifact** - notably not in the reference `hakunapi-simple-webapp-jakarta` war.
Embedding a community module in a deployment is opt-in: add it as an explicit dependency
of your own webapp.

### What belongs there

- extensions that serve a specific use case rather than the general OGC API - Features
  feature set, for example an additional output format or an alternative coordinate
  transformation implementation
- modules that are useful but are not under active development by NLS
- modules whose long-term maintenance a contributor is willing to carry

If you are unsure whether a contribution belongs in `src/` or `src-community/`, open an
[Issue](https://github.com/nlsfi/hakunapi/issues) and ask before writing the code.

### Module layout

Each community module is a normal Maven module in its own folder and must contain at
least the following:

```
src-community/<module>/
├── README.md      what the module does, how to enable it, known limitations, status
├── SUBMITTERS     the people maintaining the module, one per line, name <email>
│                  (for legacy modules, the historical authors - see below)
├── LICENSE        the module license
├── pom.xml        parent is fi.nls.hakunapi:src-community
└── src/           sources and tests
```

Optionally a `docs/` folder may be added under the module for longer documentation.

Guidelines:
- Add the module to `src-community/pom.xml` and list it in the community module table in
  the [readme](readme.md).
- Keep the module license compatible with the Hakunapi license (MIT).
- Prefer depending only on `hakunapi-core`; discuss any heavier external dependency in
  the Pull Request.
- Register runtime extensions the same way core modules do, for example an output format
  through `META-INF/services/fi.nls.hakunapi.core.OutputFormatFactorySpi`.

### Level of support for community modules

Community modules are provided as-is by their submitters. NLS reviews and merges Pull
Requests to them and keeps them building, but does not otherwise develop or support
them. A community module that stops building and finds no maintainer is dropped from the
build - removed from `src-community/pom.xml` so it no longer blocks the reactor - but the
code stays in the repository. It can be taken back into the build later by whoever is
willing to fix and maintain it.

Some modules are **legacy**: they have no active submitters at all, and the names in
their `SUBMITTERS` file are historical authors recorded for attribution rather than
people maintaining the module. Legacy does not necessarily mean unused - such a module
may well still be doing the one narrow job it was written for, which is why it is kept in
the build.
It does mean nobody is developing it, so do not expect questions about it to be answered.
If you would like to take one over, add yourself to its `SUBMITTERS` file in the same
Pull Request.
