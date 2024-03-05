import subprocess
import signal
import matplotlib.pyplot as plt
# Please uncomment line 596 in the java code and recompile before running this script
# Path to the Java program
java_program_path = "ImageDisplay"

# Image Path
image_path = "test5.rgb" 

quantization_modes = [1,2,3]

B_values = list(range(2, 257))

error_values = {1: [],2:[],3:[]}

for mode in quantization_modes:
    for B in B_values:
        command = ["java", java_program_path, image_path, str(mode), str(B**3)]

        process = subprocess.Popen(command, stdout=subprocess.PIPE, text=True)

        try:
            output, _ = process.communicate(timeout=10)
            if "Error is:" in output:
                error = int(output.split("Error is:")[1].strip())

                print("Mode ",mode,"at Bucket ",B,"error is ",error)
                error_values[mode].append(error)

        except subprocess.TimeoutExpired:
            process.send_signal(signal.SIGTERM)

plt.plot(B_values, error_values[1], label="Mode 1")
plt.plot(B_values, error_values[2], label="Mode 2")
plt.plot(B_values, error_values[3], label="Mode 3")
plt.xlabel("B values")
plt.ylabel("Absolute Error")
plt.title("Error Comparison between Mode 1 and Mode 2 and Mode 3 for "+ image_path)
plt.legend()
plt.show()
