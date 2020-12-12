My solutions for
    [Advent of Code 2020](https://adventofcode.com/2020).

They are meant to be run through
[kscript](https://github.com/holgerbrandl/kscript), passing the name of the input file like so:

```bash
kscript scr/main/kotlin/01.kts 01_input
```

Written for Kotlin 1.4 and kscript 3.0. Add Gradle 6.7 so IDEA 2020.3 gets its act together.

**Note:** Due to
[kscript#296](https://github.com/holgerbrandl/kscript/issues/296), not all files can be run with a plain kscript
command. As a workaround, use IDEA or copy functions from `shared.kt` to remove the include directive.