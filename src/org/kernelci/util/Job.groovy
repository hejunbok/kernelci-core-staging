/*
  Copyright (C) 2018 Collabora Limited
  Author: Guillaume Tucker <guillaume.tucker@collabora.com>

  This module is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.

  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
  details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/


package org.kernelci.util;

def addStrParams(params, str_params) {
    for (p in str_params) {
        params.push(
            [$class: "StringParameterValue", name: p.key, value: p.value])
    }
}

def addBoolParams(params, bool_params) {
    for (p in bool_params) {
        params.push(
            [$class: "BooleanParameterValue", name: p.key, value: p.value])
    }
}

def dockerImageName(base, cc, cc_version, kernel_arch) {
    def image_name = "${base}${cc}-${cc_version}"

    if (cc == "gcc") {
        def docker_arch
       
        if (kernel_arch == "riscv")
       	    docker_arch = "riscv64"
        else if ((kernel_arch == "i386") || (kernel_arch == "x86_64"))
            docker_arch = "x86"
        else
            docker_arch = kernel_arch

	image_name = "${image_name}_${docker_arch}"
    }

    return image_name;
}

def dockerPullWithRetry(image_name, retries=10, sleep_time=1) {
    def image = docker.image(image_name)
    def pulled = false

    while (!pulled) {
        try {
            image.pull()
            pulled = true
        }
        catch (Exception e) {
            if (!retries) {
                throw e
            }
            echo("""Docker pull failed, retry count ${retries}: ${e.toString()}""")
            sleep sleep_time
            retries -= 1
            sleep_time = sleep_time * 2
        }
    }

    return image
}
