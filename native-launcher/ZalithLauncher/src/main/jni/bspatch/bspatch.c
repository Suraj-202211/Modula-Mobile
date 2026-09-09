/*-
 * Copyright 2003-2005 Colin Percival
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted providing that the following conditions 
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#include "bzlib.h"
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <limits.h>

#define HEADER_SIZE 32

static int64_t offtin(uint8_t *buf)
{
    int64_t y;

    y = buf[7] & 0x7F;
    y = y * 256; y += buf[6];
    y = y * 256; y += buf[5];
    y = y * 256; y += buf[4];
    y = y * 256; y += buf[3];
    y = y * 256; y += buf[2];
    y = y * 256; y += buf[1];
    y = y * 256; y += buf[0];

    if (buf[7] & 0x80)
        y = -y;

    return y;
}

int bspatch_main(int argc, char *argv[])
{
    FILE *f = NULL, *cpf = NULL, *dpf = NULL, *epf = NULL;
    BZFILE *cpfbz2 = NULL, *dpfbz2 = NULL, *epfbz2 = NULL;
    int cbz2err = 0, dbz2err = 0, ebz2err = 0;
    int newfd = -1, oldfd = -1;
    int64_t oldsize = 0, newsize = 0;
    int64_t bzctrllen = 0, bzdatalen = 0;
    uint8_t header[HEADER_SIZE], buf[8];
    uint8_t *old = NULL, *new = NULL;
    int64_t oldpos = 0, newpos = 0;
    int64_t ctrl[3];
    int64_t i;
    int lenread;
    int ret = 1;

    if (argc != 4) return 1;

    /* Open patch file */
    if ((f = fopen(argv[3], "rb")) == NULL) return 2;
    if ((cpf = fopen(argv[3], "rb")) == NULL) { fclose(f); return 3; }
    if ((dpf = fopen(argv[3], "rb")) == NULL) { fclose(f); fclose(cpf); return 4; }
    if ((epf = fopen(argv[3], "rb")) == NULL) { fclose(f); fclose(cpf); fclose(dpf); return 5; }

    /* Open old file */
    if ((oldfd = open(argv[1], O_RDONLY, 0)) < 0) {
        fclose(f); fclose(cpf); fclose(dpf); fclose(epf);
        return 6;
    }

    /* Read header */
    if (fread(header, 1, HEADER_SIZE, f) < HEADER_SIZE) {
        close(oldfd); fclose(f); fclose(cpf); fclose(dpf); fclose(epf);
        return 7;
    }

    /* Check for appropriate magic: BSDIFF40 */
    if (memcmp(header, "BSDIFF40", 8) != 0) {
        close(oldfd); fclose(f); fclose(cpf); fclose(dpf); fclose(epf);
        return 8; /* Magic mismatch */
    }

    /* Read lengths from header */
    bzctrllen = offtin(header + 8);
    bzdatalen = offtin(header + 16);
    newsize = offtin(header + 24);

    if (bzctrllen < 0 || bzdatalen < 0 || newsize < 0) {
        close(oldfd); fclose(f); fclose(cpf); fclose(dpf); fclose(epf);
        return 9;
    }

    /* Close patch file handle f */
    fclose(f); f = NULL;

    /* Seek to blocks */
    int64_t offset = HEADER_SIZE;
    if (fseeko(cpf, offset, SEEK_SET)) { ret = 10; goto cleanup; }
    if ((cpfbz2 = BZ2_bzReadOpen(&cbz2err, cpf, 0, 0, NULL, 0)) == NULL) { ret = 11; goto cleanup; }

    offset += bzctrllen;
    if (fseeko(dpf, offset, SEEK_SET)) { ret = 12; goto cleanup; }
    if ((dpfbz2 = BZ2_bzReadOpen(&dbz2err, dpf, 0, 0, NULL, 0)) == NULL) { ret = 13; goto cleanup; }

    offset += bzdatalen;
    if (fseeko(epf, offset, SEEK_SET)) { ret = 14; goto cleanup; }
    if ((epfbz2 = BZ2_bzReadOpen(&ebz2err, epf, 0, 0, NULL, 0)) == NULL) { ret = 15; goto cleanup; }

    if ((oldsize = lseek(oldfd, 0, SEEK_END)) == -1) { ret = 16; goto cleanup; }
    if ((old = (uint8_t*)malloc(oldsize + 1)) == NULL) { ret = 17; goto cleanup; }
    if (lseek(oldfd, 0, SEEK_SET) != 0) { ret = 18; goto cleanup; }
    if (read(oldfd, old, oldsize) != oldsize) { ret = 19; goto cleanup; }
    close(oldfd); oldfd = -1;

    if ((new = (uint8_t*)malloc(newsize + 1)) == NULL) { ret = 20; goto cleanup; }

    oldpos = 0;
    newpos = 0;
    while (newpos < newsize) {
        /* Read control data (3 64-bit integers) */
        for (i = 0; i <= 2; i++) {
            lenread = BZ2_bzRead(&cbz2err, cpfbz2, buf, 8);
            if ((lenread < 8) || ((cbz2err != BZ_OK) && (cbz2err != BZ_STREAM_END))) {
                ret = 21;
                goto cleanup;
            }
            ctrl[i] = offtin(buf);
        }

        /* Sanity-check */
        if (ctrl[0] < 0 || ctrl[1] < 0) { ret = 22; goto cleanup; }
        if (newpos + ctrl[0] > newsize) { ret = 23; goto cleanup; }

        /* Read diff string */
        lenread = BZ2_bzRead(&dbz2err, dpfbz2, new + newpos, ctrl[0]);
        if ((lenread < ctrl[0]) || ((dbz2err != BZ_OK) && (dbz2err != BZ_STREAM_END))) {
            ret = 24;
            goto cleanup;
        }

        /* Add old data to diff string */
        for (i = 0; i < ctrl[0]; i++) {
            if ((oldpos + i >= 0) && (oldpos + i < oldsize)) {
                new[newpos + i] += old[oldpos + i];
            }
        }

        /* Adjust pointers */
        newpos += ctrl[0];
        oldpos += ctrl[0];

        /* Sanity-check */
        if (newpos + ctrl[1] > newsize) { ret = 25; goto cleanup; }

        /* Read extra string */
        lenread = BZ2_bzRead(&ebz2err, epfbz2, new + newpos, ctrl[1]);
        if ((lenread < ctrl[1]) || ((ebz2err != BZ_OK) && (ebz2err != BZ_STREAM_END))) {
            ret = 26;
            goto cleanup;
        }

        /* Adjust pointers */
        newpos += ctrl[1];
        oldpos += ctrl[2];
    }

    /* Open new file for writing */
    if ((newfd = open(argv[2], O_CREAT | O_TRUNC | O_WRONLY, 0666)) < 0) {
        ret = 27;
        goto cleanup;
    }

    if (write(newfd, new, newsize) != newsize) {
        close(newfd); newfd = -1;
        ret = 28;
        goto cleanup;
    }
    close(newfd); newfd = -1;

    ret = 0; /* SUCCESS! */

cleanup:
    if (cpfbz2) BZ2_bzReadClose(&cbz2err, cpfbz2);
    if (dpfbz2) BZ2_bzReadClose(&dbz2err, dpfbz2);
    if (epfbz2) BZ2_bzReadClose(&ebz2err, epfbz2);

    if (cpf) fclose(cpf);
    if (dpf) fclose(dpf);
    if (epf) fclose(epf);
    if (f) fclose(f);

    if (oldfd >= 0) close(oldfd);
    if (newfd >= 0) close(newfd);

    if (old) free(old);
    if (new) free(new);

    return ret;
}
