# check that the number of parameters are correct
if [ "$#" -ne 5 ]; then
    echo "ERR: Illegal number of parameters. Expected (n, max_s, min_s, delta_s, a)"
    exit 1;
fi

# compile the java class
javac SpatialAntiAliasing.java

CURR_S=$2
# loop until all values for scale factor s from max_s to min_s differing by delta_s are covered
while [ "$(echo "$CURR_S >= $3" | bc -l)" == 1 ]
do
  echo "INFO: Parameters used are n=$1, s=$CURR_S, a=$5"
  # run the compiled bytecode
  java SpatialAntiAliasing "$1" "$CURR_S" "$5"
  # update the value for scaling factor by delta_s
  # use bash numeric context to evaluate floating point expressions
  # https://www.reddit.com/r/linuxquestions/comments/61u130/linux_arithmetic_on_floating_point_variables_in/
  CURR_S=$(echo "$CURR_S - $4" | bc -l)
done

echo "INFO: Experiment terminated successfully"
exit 0
