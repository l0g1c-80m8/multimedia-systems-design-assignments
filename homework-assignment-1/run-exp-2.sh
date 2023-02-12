# check that the number of parameters are correct
if [ "$#" -ne 5 ]; then
    echo "ERR: Illegal number of parameters. Expected (n, s, max_fps, min_fps, delta_fps)"
    exit 1;
fi

# compile the java class
javac Mypart2.java

CURR_FPS=$3
# loop until all values for scale factor s from max_s to min_s differing by delta_s are covered
while [ "$(echo "$CURR_FPS >= $4" | bc -l)" == 1 ]
do
  echo "INFO: Parameters used are n=$1, s=$2, fps=$CURR_FPS"
  # run the compiled bytecode
  java Mypart2 "$1" "$2" "$CURR_FPS"
  # update the value for scaling factor by delta_s
  # use bash numeric context to evaluate floating point expressions
  # https://www.reddit.com/r/linuxquestions/comments/61u130/linux_arithmetic_on_floating_point_variables_in/
  CURR_FPS=$(echo "$CURR_FPS - $5" | bc -l)
done

echo "INFO: Experiment terminated successfully"
exit 0
