#!/usr/bin/env python3
"""Replays the shell transcripts of the manual against the current build.

Run from the repository root after ./bld build:

    python3 manual/replay.py                 # check every chapter, print the differences
    python3 manual/replay.py language.tex    # one chapter
    python3 manual/replay.py --rewrite ...   # re-record the outputs of the listed chapters

A transcript is a listing with style=shell whose lines start with "Morendo> ". The commands of
every earlier listing in the same section are run first as setup, and templates, functions and
globals defined in earlier sections are carried along; the tutorial and analysis chapters are one
continuous session each. Blocks that read the terminal, print the time or show the banner are
skipped. Expect differences only in timestamps and profiling times.
"""
import re, subprocess, sys, difflib, os
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MARK = '=====MARK====='

def balance(text):
    depth = 0; instr = False; esc = False
    for ch in text:
        if instr:
            if esc: esc = False
            elif ch == '\\': esc = True
            elif ch == '"': instr = False
        else:
            if ch == '"': instr = True
            elif ch == '(': depth += 1
            elif ch == ')': depth -= 1
    return depth

def parse(tex):
    """items: ('section', name) | ('shell', commands, expected, lineno) | ('plain', text)"""
    items = []
    lines = tex.split('\n')
    i = 0
    while i < len(lines):
        line = lines[i]
        m = re.match(r'\\(sub)?section\{', line)
        if m:
            items.append(('section', line.strip())); i += 1; continue
        m = re.match(r'\\begin\{lstlisting\}(\[(.*?)\])?', line)
        if m:
            opts = m.group(2) or ''
            start = i + 1
            j = start
            while j < len(lines) and not lines[j].startswith('\\end{lstlisting}'):
                j += 1
            body = lines[start:j]
            if 'style=shell' in opts and any(l.startswith('Morendo> ') for l in body):
                cmds = []; expected = []
                k = 0
                while k < len(body):
                    l = body[k]
                    if l.startswith('Morendo> '):
                        cmd = l[len('Morendo> '):]
                        k += 1
                        while balance(cmd) > 0 and k < len(body):
                            cmd += '\n' + body[k]; k += 1
                        cmds.append(cmd)
                    else:
                        expected.append(l); k += 1
                items.append(('shell', cmds, expected, start + 1, j + 1))
            elif 'style=java' not in opts and 'style=shell' not in opts:
                text = '\n'.join(body)
                if not re.search(r'<[a-zA-Z]', text) and '...' not in text:
                    items.append(('plain', text))
            i = j + 1; continue
        i += 1
    return items

def run(commands):
    script = '\n'.join(commands) + '\n(exit)\n'
    p = subprocess.run(['./morendo', '-shell'], input=script, capture_output=True, text=True, cwd=ROOT, timeout=180)
    return p.stdout + p.stderr

def normalize(text):
    text = text.replace('Morendo> ', '').replace('... ', '')
    out = []
    for l in text.split('\n'):
        l = l.rstrip()
        if l.strip():
            out.append(l)
    return out

CUMULATIVE = {'tutorial.tex', 'analysis.tex'}

def chunks(out, ncmds):
    """the output of each command: the text after its prompt, continuation prompts removed"""
    parts = out.split('Morendo> ')
    result = []
    for part in parts[1:1 + ncmds]:
        text = part.replace('... ', '')
        lines = [l.rstrip() for l in text.split('\n')]
        while lines and not lines[-1].strip():
            lines.pop()
        # the first line of a chunk follows the prompt directly; drop a leading blank
        while lines and not lines[0].strip():
            lines.pop(0)
        result.append(lines)
    return result

def rewrite(f, tex, items):
    """re-records every shell block: same commands, output from a real run"""
    lines = tex.split('\n')
    setup = []; carried = []
    def definitions(text):
        return [c for c in re.split(r'\n(?=\()', text) if re.match(r'\((deftemplate|deffunction|defglobal)\b', c)]
    edits = []  # (start, end, new_lines)
    for it in items:
        if it[0] == 'section':
            if f not in CUMULATIVE:
                for c in setup:
                    carried.extend(definitions(c))
                setup = []
        elif it[0] == 'plain':
            setup.append(it[1])
        elif it[0] == 'shell':
            cmds, expected, lineno, endline = it[1], it[2], it[3], it[4]
            body = lines[lineno - 1:endline - 1]
            if body and body[0].startswith('$') or any('readline t' in c or '(now)' in c for c in cmds):
                setup.extend(cmds); continue
            out = run(carried + setup + ['(printout t "%s" crlf)' % MARK] + cmds)
            after = out.split(MARK, 1)[1] if MARK in out else out
            outs = chunks(after, len(cmds))
            new = []
            for cmd, o in zip(cmds, outs):
                clines = cmd.split('\n')
                new.append('Morendo> ' + clines[0])
                new.extend(clines[1:])
                new.extend(o)
            edits.append((lineno - 1, endline - 1, new))
            setup.extend(cmds)
    for start, end, new in reversed(edits):
        lines[start:end] = new
    open(os.path.join(ROOT, 'manual/chapters', f), 'w').write('\n'.join(lines))

def main(files):
    total = 0; bad = 0
    for f in files:
        tex = open(os.path.join(ROOT, 'manual/chapters', f)).read()
        items = parse(tex)
        setup = []      # everything earlier in the same (sub)section
        carried = []    # definitions from earlier sections: templates, functions, globals
        def definitions(text):
            return [c for c in re.split(r'\n(?=\()', text) if re.match(r'\((deftemplate|deffunction|defglobal)\b', c)]
        if REWRITE:
            rewrite(f, tex, items); continue
        for it in items:
            if it[0] == 'section':
                if f in CUMULATIVE:
                    continue
                for c in setup:
                    carried.extend(definitions(c))
                setup = []
            elif it[0] == 'plain':
                setup.append(it[1])
            elif it[0] == 'shell':
                cmds, expected, lineno = it[1], it[2], it[3]
                total += 1
                out = run(carried + setup + ['(printout t "%s" crlf)' % MARK] + cmds)
                actual = out.split(MARK, 1)[1] if MARK in out else out
                a = normalize(actual)
                # drop the banner lines and the final prompt noise
                a = [l for l in a if not l.startswith('Copyright Jamocha') and not l.startswith('Morendo Version')]
                e = normalize('\n'.join(expected))
                if a != e:
                    bad += 1
                    print('=== DIFF %s:%d' % (f, lineno))
                    for d in difflib.unified_diff(e, a, 'expected', 'actual', lineterm='', n=1):
                        print(d)
                setup.extend(cmds)
    print('blocks: %d, differing: %d' % (total, bad))

REWRITE = '--rewrite' in sys.argv
args = [a for a in sys.argv[1:] if a != '--rewrite']
main(args if args else ['tutorial.tex','language.tex','getting-started.tex','queries.tex','rule-properties.tex','temporal.tex','analysis.tex','embedding.tex','java-objects.tex','molap.tex'])
